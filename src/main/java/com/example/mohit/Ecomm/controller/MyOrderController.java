package com.example.mohit.Ecomm.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.dto.OrderItemDTO;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.OrderService;
import com.example.mohit.Ecomm.service.ReturnRequestService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/my-orders")
public class MyOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReturnRequestService returnRequestService;

    // =====================================================
    // DISPLAY MY ORDERS PAGE
    // =====================================================

    @GetMapping
    public String myOrders(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Check customer login
        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/my-orders");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to view your orders.");

            return "redirect:/userlogin";
        }

        // Retrieve orders belonging to the logged-in customer
        List<OrderDTO> orders =
                orderService.getOrderByUser(
                        loggedInUser.getId());

        // =====================================================
        // CHECK RETURN ELIGIBILITY
        // =====================================================

        Set<Long> returnEligibleItemIds = new HashSet<>();

        for (OrderDTO order : orders) {

            // Only delivered orders can be eligible
            if (!"Delivered".equals(order.getStatus())
                    || order.getOrderItems() == null) {

                continue;
            }

            for (OrderItemDTO item : order.getOrderItems()) {

                if (item.getId() != null
                        && returnRequestService.canRequestReturn(
                                item.getId(),
                                loggedInUser.getId())) {

                    returnEligibleItemIds.add(item.getId());
                }
            }
        }

        // Add orders to the page
        model.addAttribute("orders", orders);

        // Add eligible order-item IDs for the Return Product button
        model.addAttribute(
                "returnEligibleItemIds",
                returnEligibleItemIds);

        // =====================================================
        // CUSTOMER RETURN REQUEST TRACKING
        // =====================================================

        model.addAttribute(
                "returnRequests",
                returnRequestService.getMyReturnRequests(
                        loggedInUser.getId()));

        return "my-orders";
    }

    // =====================================================
    // REFRESH ORDER STATUSES
    // =====================================================

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
                orderService.getOrderByUser(
                        loggedInUser.getId());

        return ResponseEntity.ok(orders);
    }

    // =====================================================
    // CANCEL ORDER
    // =====================================================

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Check customer login
        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in to cancel an order.");

            return "redirect:/userlogin";
        }

        try {

            // Cancel only the logged-in customer's own order
            orderService.cancelOrder(
                    orderId,
                    loggedInUser.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Order #" + orderId
                            + " cancelled successfully.");

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/my-orders";
    }
}