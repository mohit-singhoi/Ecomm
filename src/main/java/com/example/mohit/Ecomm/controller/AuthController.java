package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =========================================================
    // USER LOGIN PAGE
    // =========================================================

    @GetMapping("/userlogin")
    public String userLogin() {
        return "userlogin";
    }


//    // =========================================================
//    // ADMIN LOGIN PAGE
//    // =========================================================
//
//    @GetMapping("/adminlogin")
//    public String adminLogin() {
//        return "adminlogin";
//    }


    // =========================================================
    // SIGNUP PAGE
    // =========================================================

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }


    // =========================================================
    // SIGNUP PROCESS
    // =========================================================

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Check password confirmation
        if (!password.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Passwords do not match."
            );

            return "redirect:/signup";
        }


        // Check whether email already exists
        if (userRepository.findByEmail(email).isPresent()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "An account with this email already exists."
            );

            return "redirect:/signup";
        }


        // Create new user
        User user = new User();

        user.setName(name);
        user.setEmail(email);

        /*
         * IMPORTANT:
         *
         * Password is now encrypted using BCrypt.
         */
        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setRole("ROLE_USER");

        userRepository.save(user);


        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Account created successfully! Please login."
        );

        return "redirect:/userlogin";
    }


    // =========================================================
    // USER DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to access your dashboard."
            );

            return "redirect:/userlogin";
        }

        return "dashboard";
    }
}