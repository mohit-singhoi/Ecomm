package com.example.mohit.Ecomm.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.CartItem;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.CartService;
import com.example.mohit.Ecomm.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    // =====================================================
    // SHOW CHECKOUT
    // URL: GET /checkout
    // =====================================================

    @GetMapping
    public String showCheckout(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/checkout"
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue to checkout."
            );

            return "redirect:/userlogin";
        }

        Long userId = loggedInUser.getId();

        List<CartItem> cartItems =
                cartService.getCartItems(userId);

        if (cartItems == null || cartItems.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart is empty."
            );

            return "redirect:/cart";
        }

        double total = 0.0;

        List<Map<String, Object>> checkoutItems =
                new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            double itemTotal =
                    cartItem.getProduct().getPrice()
                    * cartItem.getQuantity();

            total += itemTotal;

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put("product", cartItem.getProduct());
            item.put("quantity", cartItem.getQuantity());
            item.put("itemTotal", itemTotal);

            checkoutItems.add(item);
        }

        model.addAttribute("checkoutItems", checkoutItems);
        model.addAttribute("total", total);

        return "checkout";
    }

    // =====================================================
    // PLACE ORDER
    // URL: POST /checkout/place-order
    // =====================================================

    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String pinCode,
            @RequestParam String payment,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/checkout"
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to place your order."
            );

            return "redirect:/userlogin";
        }

        Long userId = loggedInUser.getId();

        // Validate checkout details
        if (fullName == null || fullName.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || phone == null || phone.trim().isEmpty()
                || address == null || address.trim().isEmpty()
                || city == null || city.trim().isEmpty()
                || pinCode == null || pinCode.trim().isEmpty()
                || payment == null || payment.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please fill all details before placing the order."
            );

            return "redirect:/checkout";
        }

        // Get cart items
        List<CartItem> cartItems =
                cartService.getCartItems(userId);

        if (cartItems == null || cartItems.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart is empty."
            );

            return "redirect:/cart";
        }

        // Prepare product quantities
        Map<Long, Integer> productQuantities =
                new LinkedHashMap<>();

        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {

            Long productId =
                    cartItem.getProduct().getId();

            Integer quantity =
                    cartItem.getQuantity();

            productQuantities.put(productId, quantity);

            totalAmount +=
                    cartItem.getProduct().getPrice()
                    * quantity;
        }

        // Save the order using your existing service
        orderService.placeOrder(
                userId,
                productQuantities,
                totalAmount
        );

        // Clear the cart after placing the order
        cartService.clearCart(userId);

        // Prepare the payment method for display
        String displayPaymentMethod;

        String normalizedPayment = payment.trim();

        if ("COD".equalsIgnoreCase(normalizedPayment)
                || "CASH_ON_DELIVERY".equalsIgnoreCase(normalizedPayment)
                || "CASH ON DELIVERY".equalsIgnoreCase(normalizedPayment)) {

            displayPaymentMethod = "Cash on Delivery (COD)";

        } else {
            displayPaymentMethod = normalizedPayment;
        }

        // Success message
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Your order has been placed successfully!"
        );

        // Pass payment method through the redirect
        redirectAttributes.addFlashAttribute(
                "paymentMethod",
                displayPaymentMethod
        );

        return "redirect:/checkout/success";
    }

    // =====================================================
    // ORDER SUCCESS PAGE
    // URL: GET /checkout/success
    // =====================================================

    @GetMapping("/success")
    public String orderSuccess(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Protect success page
        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue."
            );

            return "redirect:/userlogin";
        }

        // Display customer name
        model.addAttribute(
                "customerName",
                loggedInUser.getName()
        );

        return "order-success";
    }
}