package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.service.ContactMessageService;

@Controller
public class ContactController {

    @Autowired
    private ContactMessageService contactMessageService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String supportEmail;

    // Open Contact Us page
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    // Submit Contact Us form
    @PostMapping("/contact/send")
    public String sendMessage(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {

        // Validate form fields
        if (name.isBlank()
                || email.isBlank()
                || subject.isBlank()
                || message.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please fill in all fields."
            );

            return "redirect:/contact";
        }

        try {
            // 1. Save customer query into the database
            contactMessageService.saveMessage(
                    name.trim(),
                    email.trim(),
                    subject.trim(),
                    message.trim()
            );

            // 2. Prepare email for ShopSphere support
            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setFrom(supportEmail);
            mail.setTo(supportEmail);
            mail.setReplyTo(email.trim());

            mail.setSubject(
                    "[ShopSphere Contact] " + subject.trim()
            );

            mail.setText(
                    "New customer query received through ShopSphere.\n\n"
                    + "Customer Name: " + name.trim() + "\n"
                    + "Customer Email: " + email.trim() + "\n"
                    + "Subject: " + subject.trim() + "\n\n"
                    + "Customer Message:\n"
                    + message.trim()
                    + "\n\n"
                    + "Please reply directly to the customer "
                    + "to respond to this query."
            );

            // 3. Send email to support
            mailSender.send(mail);

            // 4. Display success message
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thank you! Your message has been sent "
                    + "successfully. Our support team will "
                    + "contact you soon."
            );

        } catch (Exception e) {

            System.err.println(
                    "Contact form processing failed: "
                    + e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your message could not be processed. "
                    + "Please try again later."
            );
        }

        return "redirect:/contact";
    }
}