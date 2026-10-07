package com.example.mohit.Ecomm.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AccountSettingsController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountSettingsController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // ACCOUNT SETTINGS
    // =====================================================

    @GetMapping("/account-settings")
    public String accountSettings(HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // User is not logged in
        if (loggedInUser == null) {
            return "redirect:/userlogin";
        }

        return "account-settings";
    }


    // =====================================================
    // CHANGE PASSWORD PAGE
    // =====================================================

    @GetMapping("/account-settings/change-password")
    public String changePasswordPage(HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // User is not logged in
        if (loggedInUser == null) {
            return "redirect:/userlogin";
        }

        return "change-password";
    }


    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @PostMapping("/account-settings/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // User is not logged in
        if (loggedInUser == null) {
            return "redirect:/userlogin";
        }


        // -------------------------------------------------
        // Get latest user from database
        // -------------------------------------------------

        User user = userRepository.findById(loggedInUser.getId())
                .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/userlogin";
        }


        // -------------------------------------------------
        // Check current password
        // -------------------------------------------------

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Current password is incorrect.");

            return "redirect:/account-settings/change-password";
        }


        // -------------------------------------------------
        // Check new password and confirm password
        // -------------------------------------------------

        if (!newPassword.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "New password and confirm password do not match.");

            return "redirect:/account-settings/change-password";
        }


        // -------------------------------------------------
        // Check minimum password length
        // -------------------------------------------------

        if (newPassword.length() < 6) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "New password must contain at least 6 characters.");

            return "redirect:/account-settings/change-password";
        }


        // -------------------------------------------------
        // Prevent using the same password
        // -------------------------------------------------

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "New password must be different from your current password.");

            return "redirect:/account-settings/change-password";
        }


        // -------------------------------------------------
        // Encode new password using BCrypt
        // -------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);


        // -------------------------------------------------
        // Update session user
        // -------------------------------------------------

        session.setAttribute("loggedInUser", user);


        // -------------------------------------------------
        // Success message
        // -------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Password changed successfully.");

        return "redirect:/account-settings";
    }
}