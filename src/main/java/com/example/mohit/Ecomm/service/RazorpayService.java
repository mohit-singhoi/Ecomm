package com.example.mohit.Ecomm.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Create an order on Razorpay.
     *
     * @param amountInRupees amount in INR
     * @param receipt unique receipt/reference
     * @return Razorpay order details
     */
    public JSONObject createOrder(double amountInRupees, String receipt)
            throws RazorpayException {

        if (!Double.isFinite(amountInRupees) || amountInRupees <= 0) {
            throw new IllegalArgumentException("Order amount must be positive.");
        }

        // Razorpay expects the amount in paise.
        long amountInPaise = Math.round(amountInRupees * 100);

        RazorpayClient razorpayClient =
                new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        return razorpayOrder.toJson();
    }

    /**
     * Verify a Razorpay payment signature.
     */
    public boolean verifyPaymentSignature(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        if (razorpayOrderId == null || razorpayOrderId.isBlank()
                || razorpayPaymentId == null || razorpayPaymentId.isBlank()
                || razorpaySignature == null || razorpaySignature.isBlank()) {
            return false;
        }

        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;

            javax.crypto.Mac mac =
                    javax.crypto.Mac.getInstance("HmacSHA256");

            javax.crypto.spec.SecretKeySpec secretKey =
                    new javax.crypto.spec.SecretKeySpec(
                            keySecret.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8),
                            "HmacSHA256");

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    payload.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder expectedSignature = new StringBuilder();

            for (byte b : hash) {
                expectedSignature.append(
                        String.format("%02x", b & 0xff));
            }

            return java.security.MessageDigest.isEqual(
                    expectedSignature.toString().getBytes(
                            java.nio.charset.StandardCharsets.US_ASCII),
                    razorpaySignature.getBytes(
                            java.nio.charset.StandardCharsets.US_ASCII));

        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to verify Razorpay payment signature.",
                    exception);
        }
    }
    public JSONObject fetchPayment(String razorpayPaymentId)
            throws RazorpayException {

        RazorpayClient razorpayClient =
                new RazorpayClient(keyId, keySecret);

        com.razorpay.Payment payment =
                razorpayClient.payments.fetch(razorpayPaymentId);

        return payment.toJson();
    }
}