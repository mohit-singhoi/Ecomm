package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.service.SavedItemService;

@Controller
@RequestMapping("/saved-items")
public class SavedItemController {

    @Autowired
    private SavedItemService savedItemService;


    // Temporary logged-in user
    private final Long USER_ID = 1L;


    // ==========================================
    // SHOW SAVED ITEMS
    // ==========================================

    @GetMapping
    public String showSavedItems(Model model) {

        model.addAttribute(
                "savedItems",
                savedItemService.getSavedItems(USER_ID)
        );

        return "saved-items";
    }


    // ==========================================
    // MOVE TO CART
    // ==========================================

    @PostMapping("/move-to-cart/{id}")
    public String moveToCart(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            savedItemService.moveToCart(USER_ID, id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product moved to cart successfully!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/saved-items";
    }


    // ==========================================
    // REMOVE
    // ==========================================

    @PostMapping("/remove/{id}")
    public String removeSavedItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            savedItemService.removeSavedItem(USER_ID, id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product removed from saved items."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/saved-items";
    }
}