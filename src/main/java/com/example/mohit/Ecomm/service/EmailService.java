package com.example.mohit.Ecomm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ==========================================
    // PASSWORD RESET EMAIL
    // ==========================================

    public void sendPasswordResetEmail(
            String recipientEmail,
            String token) {

        String resetLink =
                baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("ShopSphere - Password Reset");

        message.setText(
                "Hello,\n\n"
                + "We received a request to reset your ShopSphere password.\n\n"
                + "Click the link below to create a new password:\n\n"
                + resetLink
                + "\n\n"
                + "This link will expire in 15 minutes.\n\n"
                + "If you did not request a password reset, "
                + "you can safely ignore this email.\n\n"
                + "Regards,\n"
                + "ShopSphere Team"
        );

        mailSender.send(message);
    }

    // ==========================================
    // CUSTOMER FEEDBACK ACKNOWLEDGEMENT EMAIL
    // Sent when the customer submits feedback
    // ==========================================

    public void sendFeedbackAcknowledgement(
            String recipientEmail,
            String customerName) {

        validateRecipientEmail(recipientEmail);

        String name = getCustomerName(customerName);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipientEmail.trim());

        message.setSubject(
                "ShopSphere - Feedback Received"
        );

        message.setText(
                "Dear " + name + ",\n\n"

                + "Thank you for sharing your feedback with ShopSphere.\n\n"

                + "We appreciate you taking the time to help us "
                + "improve our services.\n\n"

                + "Your feedback has been successfully submitted "
                + "to our customer support team.\n\n"

                + "Our team will review your feedback and respond "
                + "as soon as possible.\n\n"

                + "If you need further assistance, please contact "
                + "our support team.\n\n"

                + "Best regards,\n"
                + "ShopSphere Customer Support"
        );

        mailSender.send(message);
    }

    // ==========================================
    // ADMIN RESPONSE EMAIL
    // Sent when the admin responds to feedback
    // ==========================================

    public void sendFeedbackResponse(
            String recipientEmail,
            String customerName,
            String adminResponse) {

        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email address is missing."
            );
        }

        if (adminResponse == null || adminResponse.isBlank()) {
            throw new IllegalArgumentException(
                    "Feedback response cannot be empty."
            );
        }

        String name = (customerName == null || customerName.isBlank())
                ? "Customer"
                : customerName.trim();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipientEmail.trim());

        message.setSubject(
                "ShopSphere - Response to Your Feedback"
        );

        message.setText(
                "Dear " + name + ",\n\n"

                + "Thank you for taking the time to share your feedback with us.\n\n"

                + "We have reviewed your feedback. Your suggestions are "
                + "valuable to us and help us improve our services.\n\n"

                + "Admin's Response:\n"
                + "----------------------------------------\n"

                + adminResponse.trim()

                + "\n----------------------------------------\n\n"

                + "If you have any further questions or suggestions, "
                + "please feel free to contact us.\n\n"

                + "Best regards,\n"
                + "ShopSphere Support Team"
        );

        mailSender.send(message);
    }
    
    
 // ==========================================
 // REFUND SUCCESS EMAIL
 // Sent when a refund is successfully processed
 // ==========================================

 public void sendRefundSuccessEmail(
         String recipientEmail,
         String customerName,
         Long returnRequestId,
         Double refundAmount,
         String razorpayRefundId) {

     validateRecipientEmail(recipientEmail);

     if (refundAmount == null || refundAmount <= 0) {
         throw new IllegalArgumentException(
                 "A valid refund amount is required."
         );
     }

     String name = getCustomerName(customerName);

     SimpleMailMessage message = new SimpleMailMessage();

     message.setFrom(fromEmail);
     message.setTo(recipientEmail.trim());

     message.setSubject(
             "ShopSphere - Your Refund Has Been Successfully Processed"
     );

     message.setText(
             "Dear " + name + ",\n\n"

             + "Your refund has been successfully processed.\n\n"

             + "Here are your refund details:\n\n"

             + "Return Request ID: " + returnRequestId + "\n"

             + "Refund Amount: Rs. "
             + String.format(java.util.Locale.US, "%.2f", refundAmount)
             + "\n"

             + "Refund Status: Successfully Refunded\n"

             + "Razorpay Refund ID: "
             + ((razorpayRefundId == null || razorpayRefundId.isBlank())
                     ? "Not available"
                     : razorpayRefundId.trim())
             + "\n\n"

             + "Depending on your bank or payment provider, "
             + "it may take 3-5 working days to reflact "
             + "in your account.\n\n"

             + "Thank you for shopping with ShopSphere!\n\n"

             + "Best regards,\n"
             + "ShopSphere Support Team"
     );

     mailSender.send(message);
 }
 


    // ==========================================
    // COMMON VALIDATION
    // ==========================================

    private void validateRecipientEmail(String recipientEmail) {

        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email address is missing."
            );
        }
    }

    private String getCustomerName(String customerName) {

        if (customerName == null || customerName.isBlank()) {
            return "Customer";
        }

        return customerName.trim();
    }
}