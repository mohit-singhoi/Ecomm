package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.OrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my-orders")
public class MyOrderController {

    @Autowired
    private OrderService orderService;

    // Display the My Orders page
    @GetMapping
    public String myOrders(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            session.setAttribute("redirectAfterLogin", "/my-orders");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to view your orders."
            );

            return "redirect:/userlogin";
        }

        List<OrderDTO> orders =
                orderService.getOrderByUser(loggedInUser.getId());

        model.addAttribute("orders", orders);

        return "my-orders";
    }

    // Return the logged-in user's latest orders as JSON
    @GetMapping("/status")
    public ResponseEntity<?> getLatestOrderStatuses(
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to view your orders.");
        }

        List<OrderDTO> orders =
                orderService.getOrderByUser(loggedInUser.getId());

        return ResponseEntity.ok(orders);
    }
    
    
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Check whether the user is logged in
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in to cancel an order."
            );

            return "redirect:/userlogin";
        }

        try {
            // Cancel only the logged-in user's own order
            orderService.cancelOrder(orderId, loggedInUser.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Order #" + orderId + " cancelled successfully."
            );

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/my-orders";
    }
    
}