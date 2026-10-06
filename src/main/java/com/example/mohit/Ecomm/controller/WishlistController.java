package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.WishlistService;

import jakarta.servlet.http.HttpSession;

@Controller
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/wishlist")
    public String wishlist(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute(
                    "loggedInUser"
                );

        if (loggedInUser == null) {

            session.setAttribute(
                "redirectAfterLogin",
                "/wishlist"
            );

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Please login to view your wishlist."
            );

            return "redirect:/userlogin";
        }

        model.addAttribute(
            "wishlistItems",
            wishlistService.getWishlist(
                loggedInUser.getId()
            )
        );

        return "wishlist";
    }

    @PostMapping("/wishlist/add/{id}")
    public String addToWishlist(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute(
                    "loggedInUser"
                );

        if (loggedInUser == null) {

            session.setAttribute(
                "redirectAfterLogin",
                "/products/" + id
            );

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Please login to add products to your wishlist."
            );

            return "redirect:/userlogin";
        }

        try {

            wishlistService.addToWishlist(
                loggedInUser.getId(),
                id
            );

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product added to your wishlist!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
            );
        }

        return "redirect:/products/" + id;
    }

    @PostMapping("/wishlist/remove/{id}")
    public String removeFromWishlist(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute(
                    "loggedInUser"
                );

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Please login to continue."
            );

            return "redirect:/userlogin";
        }

        try {

            wishlistService.removeFromWishlist(
                loggedInUser.getId(),
                id
            );

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product removed from wishlist."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
            );
        }

        return "redirect:/wishlist";
    }
}