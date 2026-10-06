package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.service.ContactMessageService;

@Controller
public class ContactController {

    @Autowired
    private ContactMessageService contactMessageService;


    // Open Contact Us page

    @GetMapping("/contact")
    public String contact() {

        return "contact";
    }


    // Submit Contact Us form

    @PostMapping("/contact/send")
    public String sendMessage(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String subject,

            @RequestParam String message,

            RedirectAttributes redirectAttributes) {


        // Save message into database

        contactMessageService.saveMessage(
                name,
                email,
                subject,
                message
        );


        // Show success popup

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Thank you! Your message has been sent successfully."
        );


        return "redirect:/contact";
    }
}