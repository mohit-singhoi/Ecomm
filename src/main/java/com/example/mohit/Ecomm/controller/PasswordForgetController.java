package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.PasswordResetToken;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.PasswordResetTokenRepository;
import com.example.mohit.Ecomm.repo.UserRepository;
import com.example.mohit.Ecomm.service.EmailService;

@Controller
public class PasswordForgetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =========================================================
    // SHOW FORGOT PASSWORD PAGE
    // =========================================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }


    // =========================================================
    // SEND PASSWORD RESET EMAIL
    // =========================================================

    @PostMapping("/forgot-password")
    @Transactional
    public String sendResetLink(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(email).orElse(null);


        /*
         * Security:
         *
         * We don't tell the user whether the email exists
         * in the ShopSphere database.
         */
        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "If an account exists with this email, "
                    + "a password reset link has been sent."
            );

            return "redirect:/forgot-password";
        }


        // -----------------------------------------------------
        // Delete old reset tokens
        // -----------------------------------------------------

        passwordResetTokenRepository.deleteByUser(user);


        // -----------------------------------------------------
        // Create new reset token
        // -----------------------------------------------------

        PasswordResetToken resetToken =
                new PasswordResetToken(user);

        passwordResetTokenRepository.save(resetToken);


        // -----------------------------------------------------
        // Send email
        // -----------------------------------------------------

        try {

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    resetToken.getToken()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "If an account exists with this email, "
                    + "a password reset link has been sent."
            );

        } catch (Exception e) {

            /*
             * If email sending fails, delete the token
             * because it should not remain in the database.
             */
            passwordResetTokenRepository.deleteByToken(
                    resetToken.getToken()
            );

            System.err.println(
                    "Password reset email failed: "
                    + e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to send the reset email right now. "
                    + "Please try again later."
            );
        }


        return "redirect:/forgot-password";
    }


    // =========================================================
    // SHOW RESET PASSWORD PAGE
    // =========================================================

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token,
            RedirectAttributes redirectAttributes) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(token)
                        .orElse(null);


        // -----------------------------------------------------
        // Invalid token
        // -----------------------------------------------------

        if (resetToken == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid or expired password reset link."
            );

            return "redirect:/forgot-password";
        }


        // -----------------------------------------------------
        // Expired token
        // -----------------------------------------------------

        if (resetToken.isExpired()) {

            passwordResetTokenRepository.delete(resetToken);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This password reset link has expired. "
                    + "Please request a new one."
            );

            return "redirect:/forgot-password";
        }


        // Token is valid
        return "reset-password";
    }


    // =========================================================
    // RESET / SAVE NEW PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    @Transactional
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {


        // -----------------------------------------------------
        // Find token
        // -----------------------------------------------------

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(token)
                        .orElse(null);


        // -----------------------------------------------------
        // Invalid token
        // -----------------------------------------------------

        if (resetToken == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid or expired password reset link."
            );

            return "redirect:/forgot-password";
        }


        // -----------------------------------------------------
        // Expired token
        // -----------------------------------------------------

        if (resetToken.isExpired()) {

            passwordResetTokenRepository.delete(resetToken);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This password reset link has expired. "
                    + "Please request a new one."
            );

            return "redirect:/forgot-password";
        }


        // -----------------------------------------------------
        // Check password confirmation
        // -----------------------------------------------------

        if (!newPassword.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "New password and confirm password do not match."
            );

            return "redirect:/reset-password?token=" + token;
        }


        // -----------------------------------------------------
        // Check password length
        // -----------------------------------------------------

        if (newPassword.length() < 6) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Password must contain at least 6 characters."
            );

            return "redirect:/reset-password?token=" + token;
        }


        // -----------------------------------------------------
        // Get user
        // -----------------------------------------------------

        User user = resetToken.getUser();

        if (user == null) {

            passwordResetTokenRepository.delete(resetToken);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to reset the password. "
                    + "Please request a new reset link."
            );

            return "redirect:/forgot-password";
        }


        // -----------------------------------------------------
        // Prevent same password
        // -----------------------------------------------------

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "New password must be different "
                    + "from your current password."
            );

            return "redirect:/reset-password?token=" + token;
        }


        // -----------------------------------------------------
        // Encrypt new password using BCrypt
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);


        // -----------------------------------------------------
        // Delete token after successful password reset
        // -----------------------------------------------------

        passwordResetTokenRepository.delete(resetToken);


        // -----------------------------------------------------
        // Success
        // -----------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Password reset successfully! "
                + "You can now login with your new password."
        );


        return "redirect:/userlogin";
    }
}