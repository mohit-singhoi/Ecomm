package com.example.mohit.Ecomm.controller;

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
import com.example.mohit.Ecomm.service.CartService;
import com.example.mohit.Ecomm.service.OrderService;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;


    // Temporary logged-in user
    // Later we will get this from Login/Session
    private final Long USER_ID = 1L;


    // =====================================================
    // SHOW CHECKOUT
    // =====================================================

    @GetMapping
    public String showCheckout(Model model) {

        List<CartItem> cartItems =
                cartService.getCartItems(USER_ID);

        // If cart is empty
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }


        double total = 0.0;

        // Convert database CartItems into
        // the format used by checkout.html
        List<Map<String, Object>> checkoutItems =
                new java.util.ArrayList<>();


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

            RedirectAttributes redirectAttributes) {


        // Basic validation
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


        // Get user's database cart
        List<CartItem> cartItems =
                cartService.getCartItems(USER_ID);


        if (cartItems == null || cartItems.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart is empty."
            );

            return "redirect:/cart";
        }


        // Convert CartItems to Map<ProductId, Quantity>
        Map<Long, Integer> productQuantities =
                new LinkedHashMap<>();


        double totalAmount = 0.0;


        for (CartItem cartItem : cartItems) {

            Long productId =
                    cartItem.getProduct().getId();

            Integer quantity =
                    cartItem.getQuantity();

            productQuantities.put(
                    productId,
                    quantity
            );


            totalAmount +=
                    cartItem.getProduct().getPrice()
                    * quantity;
        }


        // Save order
        orderService.placeOrder(
                USER_ID,
                productQuantities,
                totalAmount
        );


        // Clear database cart after successful order
        cartService.clearCart(USER_ID);


        // Success message
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Your order has been placed successfully!"
        );


        return "redirect:/checkout/success";
    }


    // =====================================================
    // ORDER SUCCESS PAGE
    // =====================================================

    @GetMapping("/success")
    public String orderSuccess() {

        return "order-success";
    }
}