package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.SavedItemService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SavedItemController {

    @Autowired
    private SavedItemService savedItemService;


    // =====================================================
    // SHOW SAVED ITEMS
    // =====================================================

    @GetMapping("/saved-items")
    public String showSavedItems(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // User is not logged in
        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to view your saved items."
            );

            return "redirect:/userlogin";
        }

        // Get saved items of logged-in user
        model.addAttribute(
                "savedItems",
                savedItemService.getSavedItems(
                        loggedInUser.getId()
                )
        );

        return "saved-items";
    }


    // =====================================================
    // REMOVE SAVED ITEM
    // =====================================================

    @PostMapping("/saved-items/remove/{id}")
    public String removeSavedItem(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // User is not logged in
        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue."
            );

            return "redirect:/userlogin";
        }

        savedItemService.removeSavedItem(
                loggedInUser.getId(),
                id
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Item removed from saved items."
        );

        return "redirect:/saved-items";
    }


    // =====================================================
    // MOVE SAVED ITEM TO CART
    // =====================================================

    @PostMapping("/saved-items/move-to-cart/{id}")
    public String moveToCart(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // User is not logged in
        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue."
            );

            return "redirect:/userlogin";
        }

        savedItemService.moveToCart(
                loggedInUser.getId(),
                id
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Item moved to your cart."
        );

        return "redirect:/saved-items";
    }
}