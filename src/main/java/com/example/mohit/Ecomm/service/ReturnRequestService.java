package com.example.mohit.Ecomm.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Active or completed requests prevent another request
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

     if (orderItemId == null || userId == null) {
         throw new RuntimeException(
                 "Invalid order item or customer.");
     }

     OrderItem orderItem = orderItemRepository
             .findById(orderItemId)
             .orElseThrow(() ->
                     new RuntimeException(
                             "Order item not found."));

     Orders order = orderItem.getOrder();

     if (order == null
             || order.getUser() == null
             || !userId.equals(order.getUser().getId())) {

         throw new RuntimeException(
                 "You are not authorized to return this product.");
     }

     if (!"Delivered".equals(order.getStatus())) {
         throw new RuntimeException(
                 "Only delivered orders can be returned.");
     }

     Date deliveredAt = order.getDeliveredAt();

     if (deliveredAt == null) {
         throw new RuntimeException(
                 "The delivery date is unavailable. "
                 + "Please contact ShopSphere support.");
     }

     Instant deadline = deliveredAt.toInstant()
             .plus(RETURN_WINDOW_DAYS, ChronoUnit.DAYS);

     if (Instant.now().isAfter(deadline)) {
         throw new RuntimeException(
                 "The seven-day return window has expired.");
     }

     if (orderItem.getQuantity() <= 0) {
         throw new RuntimeException(
                 "This order item cannot be returned.");
     }

     boolean existingRequest =
             returnRequestRepository
                     .existsByOrderItem_IdAndStatusIn(
                             orderItemId,
                             BLOCKING_STATUSES);

     if (existingRequest) {
         throw new RuntimeException(
                 "A return request already exists for this product.");
     }

     // Initialize the linked product information before returning.
     orderItem.getProduct().getName();
     order.getId();

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
    // CREATE RETURN REQUEST
    // =====================================================

    @Transactional
    public ReturnRequest createReturnRequest(
            Long orderItemId,
            Long userId,
            int returnQuantity,
            String reason,
            String description) {

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
                    "The order associated with this product was not found.");
        }

        // Verify that this order belongs to the logged-in customer
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

        // Actual delivery timestamp is required
        Date deliveredAt = order.getDeliveredAt();

        if (deliveredAt == null) {
            throw new RuntimeException(
                    "The delivery date is unavailable. "
                    + "Please contact ShopSphere support.");
        }

        // Check the seven-day return window
        Instant returnDeadline = deliveredAt.toInstant()
                .plus(RETURN_WINDOW_DAYS, ChronoUnit.DAYS);

        if (Instant.now().isAfter(returnDeadline)) {
            throw new RuntimeException(
                    "The seven-day return window has expired.");
        }

        // Validate the requested quantity
        if (returnQuantity <= 0
                || returnQuantity > orderItem.getQuantity()) {

            throw new RuntimeException(
                    "Please provide a valid return quantity.");
        }

        // Validate the reason
        if (reason == null
                || !ALLOWED_REASONS.contains(reason.trim())) {

            throw new RuntimeException(
                    "Please select a valid return reason.");
        }

        // Validate the optional description
        String cleanDescription =
                description == null ? "" : description.trim();

        if (cleanDescription.length() > 1000) {
            throw new RuntimeException(
                    "The return description cannot exceed 1000 characters.");
        }

        // Prevent a duplicate active or completed request
        boolean existingRequest =
                returnRequestRepository
                        .existsByOrderItem_IdAndStatusIn(
                                orderItemId,
                                BLOCKING_STATUSES
                        );

        if (existingRequest) {
            throw new RuntimeException(
                    "A return request already exists for this product.");
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

        // Save the request
        return returnRequestRepository.save(request);
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

        if (request.getUser() == null
                || !userId.equals(request.getUser().getId())) {

            throw new RuntimeException(
                    "You are not authorized to view this return request.");
        }

        return request;
    }
}