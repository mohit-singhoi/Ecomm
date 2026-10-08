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
}