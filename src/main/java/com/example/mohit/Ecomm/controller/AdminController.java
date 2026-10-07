package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.OrderRepository;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    // =====================================================
    // ADMIN LOGIN PAGE
    // =====================================================

    @GetMapping("/adminlogin")
    public String adminLogin() {
        return "adminlogin";
    }


    // =====================================================
    // PROCESS ADMIN LOGIN
    // =====================================================

    @org.springframework.web.bind.annotation.PostMapping("/adminlogin")
    public String processAdminLogin(
            @org.springframework.web.bind.annotation.RequestParam String email,
            @org.springframework.web.bind.annotation.RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user =
                userRepository.findByEmail(email).orElse(null);

        if (user != null
                && user.getPassword().equals(password)
                && "ROLE_ADMIN".equals(user.getRole())) {

            session.setAttribute(
                    "loggedInUser",
                    user
            );

            return "redirect:/admin/dashboard";
        }

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Invalid admin credentials."
        );

        return "redirect:/adminlogin";
    }


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");


        // -------------------------------------------------
        // LOGIN CHECK
        // -------------------------------------------------

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login as administrator."
            );

            return "redirect:/adminlogin";
        }


        // -------------------------------------------------
        // ADMIN ROLE CHECK
        // -------------------------------------------------

        if (!"ROLE_ADMIN".equals(loggedInUser.getRole())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Access denied. Administrator permission required."
            );

            return "redirect:/dashboard";
        }


        // -------------------------------------------------
        // DASHBOARD STATISTICS
        // -------------------------------------------------

        long totalProducts =
                productRepository.count();

        long totalUsers =
                userRepository.count();

        long totalOrders =
                orderRepository.count();


        // -------------------------------------------------
        // TOTAL SALES
        // -------------------------------------------------

        Double totalSales =
                orderRepository.getTotalSales();

        if (totalSales == null) {
            totalSales = 0.0;
        }


        // -------------------------------------------------
        // ADD DATA TO MODEL
        // -------------------------------------------------

        model.addAttribute(
                "admin",
                loggedInUser
        );

        model.addAttribute(
                "totalProducts",
                totalProducts
        );

        model.addAttribute(
                "totalUsers",
                totalUsers
        );

        model.addAttribute(
                "totalOrders",
                totalOrders
        );

        model.addAttribute(
                "totalSales",
                totalSales
        );


        return "admin/dashboard";
    }
}