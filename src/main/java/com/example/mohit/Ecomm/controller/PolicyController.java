package com.example.mohit.Ecomm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {

    // Return, Refund & Replacement Policy Page
    @GetMapping("/return-refund-policy")
    public String returnRefundPolicy() {
        return "return-refund-policy";
    }
}