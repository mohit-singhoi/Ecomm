package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.CartItemRepository;
import com.example.mohit.Ecomm.repo.SavedItemRepository;
import com.example.mohit.Ecomm.repo.WishlistItemRepository;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class NavbarController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private SavedItemRepository savedItemRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @ModelAttribute("cartCount")
    public long cartCount(HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return 0;
        }

        return cartItemRepository.countByUser(loggedInUser);
    }

    @ModelAttribute("savedCount")
    public long savedCount(HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return 0;
        }

        return savedItemRepository
                .findByUser(loggedInUser)
                .size();
    }

    @ModelAttribute("wishlistCount")
    public long wishlistCount(HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return 0;
        }

        return wishlistItemRepository
                .countByUser(loggedInUser);
    }
}