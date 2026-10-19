package com.example.mohit.Ecomm.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.mohit.Ecomm.model.OrderItem;
import com.example.mohit.Ecomm.model.Orders;
import com.example.mohit.Ecomm.model.ReturnRequest;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.OrderItemRepository;
import com.example.mohit.Ecomm.repo.ReturnRequestRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReturnRequestService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReturnEvidenceStorageService evidenceStorageService;

    // =====================================================
    // ALLOWED RETURN REASONS
    // =====================================================

    private static final Set<String> ALLOWED_REASONS = Set.of(
            "Damaged or defective product",
            "Wrong product delivered",
            "Product differs from description",
            "Size or fit issue",
            "Other"
    );

    // These statuses prevent another return request
    // for the same order item.
    private static final List<String> BLOCKING_STATUSES = List.of(
            "REQUESTED",
            "APPROVED",
            "PICKUP_PENDING",
            "RETURN_RECEIVED",
            "REFUND_INITIATED",
            "REFUNDED"
    );

    // Proposed return window: seven days after delivery.
    private static final long RETURN_WINDOW_DAYS = 7;


    // =====================================================
    // GET AN ELIGIBLE ORDER ITEM FOR RETURN
    // =====================================================

    @Transactional
    public OrderItem getEligibleOrderItem(
            Long orderItemId,
            Long userId) {

        // Validate required IDs
        if (orderItemId == null || userId == null) {
            throw new RuntimeException(
                    "Invalid order item or customer.");
        }

        // Find the order item
        OrderItem orderItem = orderItemRepository
                .findById(orderItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order item not found."));

        // Get the associated order
        Orders order = orderItem.getOrder();

        if (order == null) {
            throw new RuntimeException(
                    "The associated order was not found.");
        }

        // Verify customer ownership
        if (order.getUser() == null
                || !userId.equals(order.getUser().getId())) {

            throw new RuntimeException(
                    "You are not authorized to return this product.");
        }

        // Only delivered orders are eligible
        if (!"Delivered".equals(order.getStatus())) {
            throw new RuntimeException(
                    "Only delivered orders can be returned.");
        }

        // Check delivery timestamp
        Date deliveredAt = order.getDeliveredAt();

        if (deliveredAt == null) {
            throw new RuntimeException(
                    "The delivery date is unavailable. "
                    + "Please contact ShopSphere support.");
        }

        // Calculate the seven-day return deadline
        Instant deadline = deliveredAt.toInstant()
                .plus(RETURN_WINDOW_DAYS, ChronoUnit.DAYS);

        if (!Instant.now().isBefore(deadline)) {
            throw new RuntimeException(
                    "The seven-day return window has expired.");
        }

        // Validate purchased quantity
        if (orderItem.getQuantity() <= 0) {
            throw new RuntimeException(
                    "This order item cannot be returned.");
        }

        // Prevent duplicate active or completed requests
        boolean existingRequest =
                returnRequestRepository
                        .existsByOrderItem_IdAndStatusIn(
                                orderItemId,
                                BLOCKING_STATUSES);

        if (existingRequest) {
            throw new RuntimeException(
                    "A return request already exists for this product.");
        }

        // Ensure product information is available
        if (orderItem.getProduct() == null) {
            throw new RuntimeException(
                    "Product information is unavailable.");
        }

        return orderItem;
    }


    // =====================================================
    // CHECK WHETHER RETURN BUTTON SHOULD BE DISPLAYED
    // =====================================================

    @Transactional
    public boolean canRequestReturn(
            Long orderItemId,
            Long userId) {

        try {
            getEligibleOrderItem(orderItemId, userId);
            return true;

        } catch (RuntimeException ex) {
            return false;
        }
    }


    // =====================================================
    // CREATE RETURN REQUEST WITH OPTIONAL EVIDENCE IMAGE
    // =====================================================

    @Transactional
    public ReturnRequest createReturnRequest(
            Long orderItemId,
            Long userId,
            int returnQuantity,
            String reason,
            String description,
            MultipartFile evidenceImage) {

        // Revalidate eligibility and customer ownership
        OrderItem orderItem =
                getEligibleOrderItem(orderItemId, userId);

        // Validate requested quantity
        if (returnQuantity <= 0
                || returnQuantity > orderItem.getQuantity()) {

            throw new RuntimeException(
                    "Please provide a valid return quantity.");
        }

        // Validate return reason
        if (reason == null
                || !ALLOWED_REASONS.contains(reason.trim())) {

            throw new RuntimeException(
                    "Please select a valid return reason.");
        }

        // Validate optional description
        String cleanDescription =
                description == null ? "" : description.trim();

        if (cleanDescription.length() > 1000) {
            throw new RuntimeException(
                    "The return description cannot exceed 1000 characters.");
        }

        // Find the customer
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found."));

        // Create the return request
        ReturnRequest request = new ReturnRequest();

        request.setUser(user);
        request.setOrderItem(orderItem);
        request.setReturnQuantity(returnQuantity);
        request.setReason(reason.trim());
        request.setDescription(cleanDescription);
        request.setStatus("REQUESTED");
        request.setRefundStatus("NOT_APPLICABLE");

        // Store the optional evidence image
        String savedImageFilename =
                evidenceStorageService.store(evidenceImage);

        request.setEvidenceImagePath(savedImageFilename);

        try {

            // Flush to detect database errors before returning
            return returnRequestRepository.saveAndFlush(request);

        } catch (RuntimeException ex) {

            // Remove the image if saving fails
            evidenceStorageService.delete(savedImageFilename);

            throw ex;
        }
    }


    // =====================================================
    // GET RETURN REQUESTS FOR THE LOGGED-IN CUSTOMER
    // =====================================================

    @Transactional
    public List<ReturnRequest> getMyReturnRequests(Long userId) {

        if (userId == null) {
            throw new RuntimeException(
                    "Please log in to view your return requests.");
        }

        return userRepository.findById(userId)
                .map(user ->
                        returnRequestRepository
                                .findByUser_IdOrderByRequestDateDesc(
                                        user.getId()))
                .orElseThrow(() ->
                        new RuntimeException("User not found."));
    }


    // =====================================================
    // GET RETURN REQUEST BY ID FOR ITS OWNER
    // =====================================================

    @Transactional
    public ReturnRequest getMyReturnRequest(
            Long requestId,
            Long userId) {

        if (requestId == null || userId == null) {
            throw new RuntimeException(
                    "Invalid return request.");
        }

        ReturnRequest request = returnRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Return request not found."));

        // Verify customer ownership
        if (request.getUser() == null
                || !userId.equals(request.getUser().getId())) {

            throw new RuntimeException(
                    "You are not authorized to view this return request.");
        }

        return request;
    }


    // =====================================================
    // ADMIN: GET ALL RETURN REQUESTS
    // =====================================================

    @Transactional
    public List<ReturnRequest> getAllReturnRequestsForAdmin() {

        return returnRequestRepository
                .findAllByOrderByRequestDateDesc();
    }


    // =====================================================
    // ADMIN: GET ONE RETURN REQUEST WITH DETAILS
    // =====================================================

    @Transactional
    public ReturnRequest getReturnRequestForAdmin(
            Long requestId) {

        if (requestId == null) {
            throw new RuntimeException(
                    "Invalid return request ID.");
        }

        return returnRequestRepository
                .findDetailedById(requestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Return request not found."));
    }


    // =====================================================
    // ADMIN: APPROVE OR REJECT A RETURN REQUEST
    // =====================================================

    @Transactional
    public ReturnRequest reviewReturnRequest(
            Long requestId,
            String decision,
            String adminRemarks) {

        // Find the request with its associated details
        ReturnRequest request =
                getReturnRequestForAdmin(requestId);

        // Only requests awaiting review can be reviewed
        if (!"REQUESTED".equals(request.getStatus())) {
            throw new RuntimeException(
                    "This return request has already been reviewed.");
        }

        // Validate decision
        if (decision == null || decision.isBlank()) {
            throw new RuntimeException(
                    "Please select Approve or Reject.");
        }

        String normalizedDecision =
                decision.trim().toUpperCase(Locale.ROOT);

        if (!"APPROVE".equals(normalizedDecision)
                && !"REJECT".equals(normalizedDecision)) {

            throw new RuntimeException(
                    "Invalid return decision.");
        }

        // Validate administrator remarks
        String remarks =
                adminRemarks == null ? "" : adminRemarks.trim();

        if (remarks.length() > 1000) {
            throw new RuntimeException(
                    "Admin remarks cannot exceed 1000 characters.");
        }

        // A rejection must include an explanation
        if ("REJECT".equals(normalizedDecision)
                && remarks.isBlank()) {

            throw new RuntimeException(
                    "Please provide a reason for rejecting the request.");
        }

        // Save the decision
        if ("APPROVE".equals(normalizedDecision)) {

            request.setStatus("APPROVED");

            if (remarks.isBlank()) {
                remarks =
                        "Your return request has been approved.";
            }

        } else {

            request.setStatus("REJECTED");
        }

        request.setAdminRemarks(remarks);
        request.setDecisionDate(new Date());

        // Approval does not itself initiate a refund.
        // Refund processing is implemented separately.

        return returnRequestRepository.save(request);
    }

}