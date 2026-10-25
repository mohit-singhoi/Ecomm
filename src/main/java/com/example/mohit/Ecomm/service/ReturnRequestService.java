package com.example.mohit.Ecomm.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.json.JSONObject;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;

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
import com.example.mohit.Ecomm.service.RazorpayService;

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
    
    @Autowired
    private RazorpayService razorpayService;
    
    @Autowired
    private EmailService emailService;

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
    
 // =====================================================
 // ADMIN: ADVANCE RETURN PICKUP STATUS
 // =====================================================

 @Transactional
 public ReturnRequest advanceReturnStatus(
         Long requestId,
         String nextStatus,
         String adminRemarks) {

     if (requestId == null) {
         throw new RuntimeException(
                 "Invalid return request ID.");
     }

     ReturnRequest request = returnRequestRepository
             .findById(requestId)
             .orElseThrow(() ->
                     new RuntimeException(
                             "Return request not found."));

     String currentStatus = request.getStatus();

     if (nextStatus == null || nextStatus.isBlank()) {
         throw new RuntimeException(
                 "Please select the next return status.");
     }

     String targetStatus =
             nextStatus.trim().toUpperCase(Locale.ROOT);

     // Allow only valid sequential transitions.
     if ("APPROVED".equals(currentStatus)
             && "PICKUP_PENDING".equals(targetStatus)) {

         // Admin is starting the pickup coordination process.

     } else if ("PICKUP_PENDING".equals(currentStatus)
             && "RETURN_RECEIVED".equals(targetStatus)) {

         // Admin confirms that the returned product was received.

     } else {
         throw new RuntimeException(
                 "Invalid return status transition from "
                         + currentStatus + " to " + targetStatus + ".");
     }

     String remarks =
             adminRemarks == null ? "" : adminRemarks.trim();

     if (remarks.length() > 1000) {
         throw new RuntimeException(
                 "Admin remarks cannot exceed 1000 characters.");
     }

     if (remarks.isBlank()) {

         if ("PICKUP_PENDING".equals(targetStatus)) {
             remarks =
                     "Your return has been approved. "
                     + "Pickup is being arranged. "
                     + "ShopSphere will provide further instructions.";
         } else {
             remarks =
                     "ShopSphere has received your returned product. "
                     + "The refund will be reviewed next, if eligible.";
         }
     }

     request.setStatus(targetStatus);
     request.setAdminRemarks(remarks);

     return returnRequestRepository.save(request);
 }
 
//=====================================================
//SEND SUCCESSFUL REFUND EMAIL ONCE
//=====================================================
private void sendRefundSuccessEmailIfNeeded(ReturnRequest request) {

  if (!"REFUNDED".equals(request.getRefundStatus())) {
      return;
  }

  if (request.isRefundEmailSent()) {
      return;
  }

  User customer = request.getUser();

  if (customer == null
          || customer.getEmail() == null
          || customer.getEmail().isBlank()) {
      return;
  }

  OrderItem item = request.getOrderItem();

  try {
      emailService.sendRefundSuccessEmail(
              customer.getEmail(),
              customer.getName(),
              request.getId(),
              request.getRefundAmount(),
              request.getRazorpayRefundId()
      );

      request.setRefundEmailSent(true);

  } catch (Exception ex) {
      // Do not change the successful refund status if email fails.
      request.setAdminRemarks(
              "Refund processed successfully, but the customer "
              + "notification email could not be sent."
      );
  }
}

//=====================================================
//ADMIN: INITIATE REFUND AFTER RETURN IS RECEIVED
//=====================================================

@Transactional
public ReturnRequest initiateRefund(Long requestId) {

  if (requestId == null) {
      throw new RuntimeException("Invalid return request ID.");
  }

  ReturnRequest request = getReturnRequestForAdmin(requestId);

  // 1. Validate return status.
  if (!"RETURN_RECEIVED".equals(request.getStatus())) {
      throw new RuntimeException(
              "The returned product must be received before refunding.");
  }

  // 2. Prevent duplicate or unsafe retry attempts.
  String refundStatus = request.getRefundStatus();

  if ("INITIATED".equals(refundStatus)
          || "REFUNDED".equals(refundStatus)
          || "FAILED".equals(refundStatus)) {

      throw new RuntimeException(
              "A refund attempt already exists. Verify its status "
              + "in Razorpay before taking further action.");
  }

  OrderItem item = request.getOrderItem();

  if (item == null || item.getOrder() == null) {
      throw new RuntimeException(
              "Order information is unavailable.");
  }

  Orders order = item.getOrder();

  // 3. Only process online Razorpay payments here.
  if (!"ONLINE".equalsIgnoreCase(order.getPaymentMethod())) {
      throw new RuntimeException(
              "This is not an online payment. "
              + "Please process the COD refund separately.");
  }

  if (!"PAID".equalsIgnoreCase(order.getPaymentStatus())) {
      throw new RuntimeException(
              "The original payment has not been confirmed as paid.");
  }

  String paymentId = order.getRazorpayPaymentId();

  if (paymentId == null || paymentId.isBlank()) {
      throw new RuntimeException(
              "The original Razorpay payment ID is unavailable.");
  }

  // 4. Calculate the refund using the original purchase price.
  Double purchasePrice = item.getPurchasePrice();

  if (purchasePrice == null
          || !Double.isFinite(purchasePrice)
          || purchasePrice <= 0) {

      throw new RuntimeException(
              "The original purchase price is unavailable.");
  }

  int returnQuantity = request.getReturnQuantity();

  if (returnQuantity <= 0
          || returnQuantity > item.getQuantity()) {

      throw new RuntimeException("Invalid return quantity.");
  }

  BigDecimal refundAmount = BigDecimal.valueOf(purchasePrice)
          .multiply(BigDecimal.valueOf(returnQuantity))
          .setScale(2, RoundingMode.HALF_UP);

  long refundAmountInPaise = refundAmount
          .multiply(BigDecimal.valueOf(100))
          .longValueExact();

  if (refundAmountInPaise <= 0) {
      throw new RuntimeException(
              "The calculated refund amount is invalid.");
  }

  try {

      // 5. Retrieve the original payment details.
      JSONObject payment = razorpayService.fetchPayment(paymentId);

      long originalPaymentAmount =
              payment.getLong("amount");

      if (originalPaymentAmount <= 0) {
          throw new RuntimeException(
                  "The original payment amount is invalid.");
      }

      if (refundAmountInPaise > originalPaymentAmount) {
          throw new RuntimeException(
                  "The refund amount exceeds the original payment.");
      }

      // 6. Retrieve existing refunds for this payment.
      List<Refund> existingRefunds =
              razorpayService.fetchRefunds(paymentId);

      long reservedRefundAmount = 0;

      for (Refund existingRefund : existingRefunds) {

          JSONObject refundJson = existingRefund.toJson();

          String status = refundJson.optString("status", "");

          // Failed refunds do not reserve the refundable balance.
          // Pending, processed, and unknown statuses are conservatively
          // counted to avoid issuing an excessive refund.
          if (!"failed".equalsIgnoreCase(status)) {

              long existingAmount = refundJson.getLong("amount");

              if (existingAmount > 0) {
                  reservedRefundAmount = Math.addExact(
                          reservedRefundAmount,
                          existingAmount);
              }
          }
      }

      // Include Razorpay's reported refunded amount as well.
      long amountRefundedByPayment =
              payment.optLong("amount_refunded", 0);

      if (amountRefundedByPayment < 0) {
          throw new RuntimeException(
                  "Razorpay returned an invalid refunded amount.");
      }

      // Use the greater figure rather than adding the two values,
      // because the payment total may already include listed refunds.
      long alreadyRefundedOrReserved = Math.max(
              reservedRefundAmount,
              amountRefundedByPayment);

      long remainingRefundableAmount = Math.subtractExact(
              originalPaymentAmount,
              alreadyRefundedOrReserved);

      if (remainingRefundableAmount < 0) {
          throw new RuntimeException(
                  "Existing refunds exceed the original payment. "
                  + "Please reconcile the payment in Razorpay.");
      }

      if (refundAmountInPaise > remainingRefundableAmount) {
          throw new RuntimeException(
                  "The requested refund exceeds the remaining "
                  + "refundable balance. Remaining amount: ₹"
                  + BigDecimal.valueOf(remainingRefundableAmount, 2));
      }

      // 7. Save the intent before submitting the refund.
      request.setRefundAmount(refundAmount.doubleValue());
      request.setRefundStatus("INITIATED");

      returnRequestRepository.saveAndFlush(request);

      // 8. Submit the refund to Razorpay.
      JSONObject refundResponse = razorpayService.initiateRefund(
              paymentId,
              refundAmountInPaise);

      String refundId = refundResponse.optString("id", "");

      if (refundId.isBlank()) {
          request.setAdminRemarks(
                  "The refund response did not contain a refund ID. "
                  + "Verify the refund in Razorpay before proceeding.");

          return returnRequestRepository.save(request);
      }

      request.setRazorpayRefundId(refundId);

      String providerStatus =
              refundResponse.optString("status", "");

      if ("processed".equalsIgnoreCase(providerStatus)) {

    	    request.setRefundStatus("REFUNDED");

    	    request.setAdminRemarks(
    	            "Razorpay reports that the refund has been processed.");

    	    sendRefundSuccessEmailIfNeeded(request);
    	} else if ("failed".equalsIgnoreCase(providerStatus)) {

          request.setRefundStatus("FAILED");

          request.setAdminRemarks(
                  "Razorpay reports a failed refund. "
                  + "Verify the provider status before any retry.");

      } else {

          request.setRefundStatus("INITIATED");

          request.setAdminRemarks(
                  "Refund submitted to Razorpay. "
                  + "Awaiting confirmation of completion.");
      }

      return returnRequestRepository.save(request);

  } catch (RazorpayException ex) {

      // A failed API response does not necessarily mean that Razorpay
      // rejected the refund. Do not submit it again automatically.
      request.setRefundStatus("INITIATED");

      request.setAdminRemarks(
              "The refund outcome could not be confirmed. "
              + "Check Razorpay before taking further action.");

      return returnRequestRepository.save(request);

  } catch (ArithmeticException ex) {

      throw new RuntimeException(
              "The refund calculation exceeded the supported amount range.",
              ex);
  }
}

//=====================================================
//ADMIN: SYNCHRONIZE REFUND STATUS WITH RAZORPAY
//=====================================================
@Transactional
public ReturnRequest synchronizeRefundStatus(Long requestId) {


if (requestId == null) {
 throw new RuntimeException("Invalid return request ID.");
}

ReturnRequest request = getReturnRequestForAdmin(requestId);

if (!"RETURN_RECEIVED".equals(request.getStatus())) {
 throw new RuntimeException(
         "The returned product must be received before checking its refund.");
}

String refundId = request.getRazorpayRefundId();

if (refundId == null || refundId.isBlank()) {
 throw new RuntimeException(
         "No Razorpay refund ID is available for this return request.");
}

try {
 JSONObject refundResponse =
         razorpayService.fetchRefund(refundId);

 String providerStatus =
         refundResponse.optString("status", "")
                 .trim()
                 .toLowerCase(Locale.ROOT);

 switch (providerStatus) {
     case "processed":
         request.setRefundStatus("REFUNDED");
         request.setAdminRemarks(
                 "Razorpay confirms that the refund has been processed.");
         sendRefundSuccessEmailIfNeeded(request);
         break;

     case "failed":
         request.setRefundStatus("FAILED");
         request.setAdminRemarks(
                 "Razorpay reports that the refund failed. "
                 + "Verify the provider details before attempting another refund.");
         break;

     case "pending":
     case "queued":
     case "created":
         request.setRefundStatus("INITIATED");
         request.setAdminRemarks(
                 "Razorpay is still processing the refund.");
         break;

     default:
         throw new RuntimeException(
                 "Razorpay returned an unrecognized refund status: "
                         + providerStatus);
 }

 return returnRequestRepository.save(request);

} catch (RazorpayException ex) {
 throw new RuntimeException(
         "Unable to retrieve the refund status from Razorpay. "
         + "Your existing refund status has not been deliberately changed.",
         ex);
}


}

//=====================================================
//ADMIN: RECORD COD MANUAL REFUND
//=====================================================

@Transactional
public ReturnRequest recordCodRefund(
     Long requestId,
     String refundMethod,
     String refundReference,
     Date refundDate,
     BigDecimal refundAmount) {

 // 1. Validate request ID.
 if (requestId == null) {
     throw new RuntimeException("Invalid return request ID.");
 }

 // 2. Load the return request.
 ReturnRequest request = getReturnRequestForAdmin(requestId);

 // 3. The returned product must have been received.
 if (!"RETURN_RECEIVED".equals(request.getStatus())) {
     throw new RuntimeException(
             "The returned product must be received before recording a refund.");
 }

 // 4. Load order details.
 OrderItem item = request.getOrderItem();

 if (item == null || item.getOrder() == null) {
     throw new RuntimeException("Order information is unavailable.");
 }

 Orders order = item.getOrder();

 // 5. Only COD orders can use this method.
 String paymentMethod = order.getPaymentMethod();

 if (paymentMethod == null
         || !("COD".equalsIgnoreCase(paymentMethod.trim())
         || "CASH_ON_DELIVERY".equalsIgnoreCase(paymentMethod.trim()))) {

     throw new RuntimeException(
             "This refund method is only for Cash on Delivery orders.");
 }

 // 6. Prevent duplicate refunds.
 if (request.isCodRefundRecorded()
         || "REFUNDED".equalsIgnoreCase(request.getRefundStatus())) {

     throw new RuntimeException(
             "A refund has already been recorded for this request.");
 }

 // Do not use the COD method for an existing online refund.
 if (request.getRazorpayRefundId() != null
         && !request.getRazorpayRefundId().isBlank()) {

     throw new RuntimeException(
             "A Razorpay refund already exists for this request.");
 }

 // 7. Validate refund method.
 if (refundMethod == null || refundMethod.isBlank()) {
     throw new RuntimeException("Please select a refund method.");
 }

 String normalizedMethod =
         refundMethod.trim().toUpperCase(Locale.ROOT);

 if (!Set.of("UPI", "BANK_TRANSFER", "OTHER")
         .contains(normalizedMethod)) {

     throw new RuntimeException(
             "Invalid refund method. Choose UPI, bank transfer, or other.");
 }

 // 8. Validate transaction reference / UTR.
 if (refundReference == null || refundReference.isBlank()) {
     throw new RuntimeException(
             "Please enter the UTR or transaction reference.");
 }

 String cleanReference = refundReference.trim();

 if (cleanReference.length() > 150) {
     throw new RuntimeException(
             "The refund reference cannot exceed 150 characters.");
 }

 // 9. Validate refund date.
 if (refundDate == null) {
     throw new RuntimeException("Please provide the refund date.");
 }

 if (refundDate.after(new Date())) {
     throw new RuntimeException(
             "The refund date cannot be in the future.");
 }

 // 10. Calculate the maximum refundable amount.
 Double purchasePrice = item.getPurchasePrice();

 if (purchasePrice == null
         || !Double.isFinite(purchasePrice)
         || purchasePrice <= 0) {

     throw new RuntimeException(
             "The original purchase price is unavailable.");
 }

 int returnQuantity = request.getReturnQuantity();

 if (returnQuantity <= 0
         || returnQuantity > item.getQuantity()) {

     throw new RuntimeException("Invalid return quantity.");
 }

 BigDecimal maximumRefund = BigDecimal.valueOf(purchasePrice)
         .multiply(BigDecimal.valueOf(returnQuantity))
         .setScale(2, RoundingMode.HALF_UP);

 // 11. Validate the actual refund amount.
 if (refundAmount == null) {
     throw new RuntimeException("Please enter the refund amount.");
 }

 BigDecimal actualRefund = refundAmount.setScale(
         2, RoundingMode.UNNECESSARY);

 if (actualRefund.compareTo(BigDecimal.ZERO) <= 0) {
     throw new RuntimeException(
             "The refund amount must be greater than zero.");
 }

 if (actualRefund.compareTo(maximumRefund) > 0) {
     throw new RuntimeException(
             "The refund cannot exceed the refundable product amount of ₹"
                     + maximumRefund);
 }

 // 12. Record the refund already sent by the administrator.
 request.setCodRefundMethod(normalizedMethod);
 request.setCodRefundReference(cleanReference);
 request.setCodRefundDate(refundDate);
 request.setRefundAmount(actualRefund.doubleValue());
 request.setCodRefundRecorded(true);
 request.setRefundStatus("REFUNDED");

 request.setAdminRemarks(
         "COD refund recorded by administrator. "
         + "Method: " + normalizedMethod
         + ". Reference: " + cleanReference + ".");

 // 13. Save the refund details.
 ReturnRequest savedRequest =
         returnRequestRepository.saveAndFlush(request);

 // 14. Send the success email if your email method is available.
 sendRefundSuccessEmailIfNeeded(savedRequest);

 return returnRequestRepository.save(savedRequest);
}


}