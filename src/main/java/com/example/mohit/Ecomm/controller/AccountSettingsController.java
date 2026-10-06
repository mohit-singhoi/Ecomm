package com.example.mohit.Ecomm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.mohit.Ecomm.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class AccountSettingsController {

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
}