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
    // CUSTOMER FEEDBACK RESPONSE EMAIL
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
                + "Thank you for sharing your feedback with ShopSphere. "
                + "We appreciate you taking the time to help us improve.\n\n"
                + "Our customer support team has responded to your feedback:\n\n"
                + "----------------------------------------\n"
                + adminResponse.trim() + "\n"
                + "----------------------------------------\n\n"
                + "If you need further assistance, please contact our support team.\n\n"
                + "Best regards,\n"
                + "ShopSphere Customer Support"
        );

        mailSender.send(message);
    }
}