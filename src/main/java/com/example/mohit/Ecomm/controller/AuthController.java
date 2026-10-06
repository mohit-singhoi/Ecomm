package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
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


    // =====================================================
    // USER LOGIN PAGE
    // =====================================================

    @GetMapping("/userlogin")
    public String userLogin() {

        return "userlogin";
    }


    // =====================================================
    // PROCESS USER LOGIN
    // =====================================================

    @PostMapping("/userlogin")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Find user by email
        User user =
                userRepository.findByEmail(email).orElse(null);


        // =================================================
        // CHECK EMAIL AND PASSWORD
        // =================================================

        if (user != null && user.getPassword().equals(password)) {

            // Store logged-in user in session
            session.setAttribute(
                    "loggedInUser",
                    user
            );


            // =================================================
            // CHECK ORIGINAL PAGE
            // =================================================

            String redirectAfterLogin =
                    (String) session.getAttribute(
                            "redirectAfterLogin"
                    );


            /*
             * If the user was redirected to login from
             * another protected page, send them back there.
             */
            if (redirectAfterLogin != null
                    && !redirectAfterLogin.isBlank()) {

                // Remove it after using it
                session.removeAttribute(
                        "redirectAfterLogin"
                );

                // Redirect to original requested page
                return "redirect:" + redirectAfterLogin;
            }


            // =================================================
            // NORMAL LOGIN
            // =================================================

            return "redirect:/dashboard";
        }


        // =====================================================
        // INVALID LOGIN
        // =====================================================

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Invalid email or password."
        );

        return "redirect:/userlogin";
    }


    // =====================================================
    // SIGNUP PAGE
    // =====================================================

    @GetMapping("/signup")
    public String signup() {

        return "signup";
    }


    // =====================================================
    // PROCESS SIGNUP
    // =====================================================

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {


        // =================================================
        // CHECK PASSWORD
        // =================================================

        if (!password.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Passwords do not match."
            );

            return "redirect:/signup";
        }


        // =================================================
        // CHECK DUPLICATE EMAIL
        // =================================================

        if (userRepository.findByEmail(email).isPresent()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "An account with this email already exists."
            );

            return "redirect:/signup";
        }


        // =================================================
        // CREATE NEW USER
        // =================================================

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        // Normal signup user
        user.setRole("ROLE_USER");


        // Save user
        userRepository.save(user);


        // =================================================
        // SUCCESS MESSAGE
        // =================================================

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Account created successfully! Please login."
        );

        return "redirect:/userlogin";
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @GetMapping("/logout")
    public String logout(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Destroy login session
        session.invalidate();


        // Success message
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "You have been logged out successfully."
        );

        return "redirect:/userlogin";
    }


    // =====================================================
    // DASHBOARD
    // =====================================================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            RedirectAttributes redirectAttributes) {


        // Get logged-in user
        User loggedInUser =
                (User) session.getAttribute(
                        "loggedInUser"
                );


        // =================================================
        // NOT LOGGED IN
        // =================================================

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to access your dashboard."
            );

            return "redirect:/userlogin";
        }


        // =================================================
        // LOGGED IN
        // =================================================

        return "dashboard";
    }
}