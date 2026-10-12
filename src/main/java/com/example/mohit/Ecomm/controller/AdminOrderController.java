package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.Orders;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.OrderRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    // Check administrator access
    private boolean isAdmin(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");

        return admin != null
                && "ROLE_ADMIN".equals(admin.getRole());
    }

    // Display all orders
    @GetMapping
    public String listOrders(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator."
            );

            return "redirect:/adminlogin";
        }

        List<Orders> orders = orderRepository.findAllOrderWithUsers();

        model.addAttribute("orders", orders);
        model.addAttribute("activePage", "orders");

        return "admin/orders";
    }

    // Display details of an individual order
    @GetMapping("/view/{id}")
    public String viewOrder(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator."
            );

            return "redirect:/adminlogin";
        }

        Orders order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Order not found."
            );

            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("activePage", "orders");

        return "admin/order-details";
    }

    // Update order status
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator."
            );

            return "redirect:/adminlogin";
        }

        Orders order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Order not found."
            );

            return "redirect:/admin/orders";
        }

        List<String> allowedStatuses = List.of(
                "Pending",
                "Processing",
                "Shipped",
                "Delivered",
                "Cancelled"
        );

        if (!allowedStatuses.contains(status)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid order status."
            );

            return "redirect:/admin/orders/view/" + id;
        }

        order.setStatus(status);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Order #" + id + " status updated successfully."
        );

        return "redirect:/admin/orders/view/" + id;
    }
}