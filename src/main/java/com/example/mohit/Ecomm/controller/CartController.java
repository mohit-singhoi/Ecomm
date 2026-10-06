package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.CartItem;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.CartService;
import com.example.mohit.Ecomm.service.SavedItemService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private SavedItemService savedItemService;


    // =========================================================
    // SHOW CART
    // URL: GET /cart
    // =========================================================

    @GetMapping
    public String showCart(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // USER NOT LOGGED IN
        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/cart"
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to view your cart."
            );

            return "redirect:/userlogin";
        }

        Long userId = loggedInUser.getId();

        List<CartItem> cartItems =
                cartService.getCartItems(userId);

        double total = 0.0;

        for (CartItem item : cartItems) {

            total +=
                    item.getProduct().getPrice()
                    * item.getQuantity();
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);

        return "cart";
    }


    // =========================================================
    // ADD TO CART
    // URL: POST /cart/add/{id}
    // =========================================================

    @PostMapping("/add/{id}")
    public String addToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/products/" + id
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to add products to your cart."
            );

            return "redirect:/userlogin";
        }

        try {

            if (quantity < 1) {
                quantity = 1;
            }

            cartService.addToCart(
                    loggedInUser.getId(),
                    id,
                    quantity
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product added to cart successfully!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }


    // =========================================================
    // BUY NOW
    // URL: POST /cart/buy-now/{id}
    // =========================================================

    @PostMapping("/buy-now/{id}")
    public String buyNow(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/products/" + id
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue."
            );

            return "redirect:/userlogin";
        }

        try {

            if (quantity < 1) {
                quantity = 1;
            }

            cartService.addToCart(
                    loggedInUser.getId(),
                    id,
                    quantity
            );

            return "redirect:/checkout";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/products/" + id;
        }
    }


    // =========================================================
    // UPDATE CART QUANTITY
    // URL: POST /cart/update/{id}
    // =========================================================

    @PostMapping("/update/{id}")
    public String updateCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/cart"
            );

            return "redirect:/userlogin";
        }

        try {

            cartService.updateQuantity(
                    loggedInUser.getId(),
                    id,
                    quantity
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cart quantity updated successfully!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }


    // =========================================================
    // SAVE FOR LATER
    // URL: POST /cart/save-for-later/{id}
    // =========================================================

    @PostMapping("/save-for-later/{id}")
    public String saveForLater(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/cart"
            );

            return "redirect:/userlogin";
        }

        try {

            savedItemService.saveForLater(
                    loggedInUser.getId(),
                    id
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product saved for later successfully!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }


    // =========================================================
    // REMOVE ITEM FROM CART
    // URL: POST /cart/remove/{id}
    // =========================================================

    @PostMapping("/remove/{id}")
    public String removeFromCart(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/cart"
            );

            return "redirect:/userlogin";
        }

        try {

            cartService.removeItem(
                    loggedInUser.getId(),
                    id
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product removed from cart."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }


    // =========================================================
    // CLEAR CART
    // URL: POST /cart/clear
    // =========================================================

    @PostMapping("/clear")
    public String clearCart(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/cart"
            );

            return "redirect:/userlogin";
        }

        try {

            cartService.clearCart(
                    loggedInUser.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cart cleared successfully."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }
}