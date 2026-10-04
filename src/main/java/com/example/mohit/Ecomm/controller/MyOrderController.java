package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.service.OrderService;

@Controller
@RequestMapping("/my-orders")
public class MyOrderController {

    @Autowired
    private OrderService orderService;


    @GetMapping
    public String myOrders(Model model) {

        // Temporary user ID
        // Later this will come from logged-in user session
        Long userId = 1L;

        List<OrderDTO> orders =
                orderService.getOrderByUser(userId);

        model.addAttribute("orders", orders);

        return "my-orders";
    }
}