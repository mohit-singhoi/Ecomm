package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my-orders")
public class MyOrderController {

    @Autowired
    private OrderService orderService;


    // =====================================================
    // MY ORDERS
    // URL: GET /my-orders
    // =====================================================

    @GetMapping
    public String myOrders(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {


        // =================================================
        // GET LOGGED-IN USER
        // =================================================

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");


        // =================================================
        // USER NOT LOGGED IN
        // =================================================

        if (loggedInUser == null) {

            // Remember requested page
            session.setAttribute(
                    "redirectAfterLogin",
                    "/my-orders"
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to view your orders."
            );

            return "redirect:/userlogin";
        }


        // =================================================
        // GET USER ID
        // =================================================

        Long userId =
                loggedInUser.getId();


        // =================================================
        // GET USER ORDERS
        // =================================================

        List<OrderDTO> orders =
                orderService.getOrderByUser(userId);


        // =================================================
        // SEND ORDERS TO PAGE
        // =================================================

        model.addAttribute(
                "orders",
                orders
        );


        return "my-orders";
    }
}