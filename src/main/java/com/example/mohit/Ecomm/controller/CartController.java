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
import com.example.mohit.Ecomm.service.CartService;
import com.example.mohit.Ecomm.service.SavedItemService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private SavedItemService savedItemService;

    /*
     * Temporary user ID.
     *
     * Later, when login/authentication is implemented,
     * this will be replaced with the logged-in user's ID.
     */
    private final Long USER_ID = 1L;


    // =========================================================
    // SHOW CART
    // URL: GET /cart
    // =========================================================

    @GetMapping
    public String showCart(Model model) {

        List<CartItem> cartItems =
                cartService.getCartItems(USER_ID);

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
            RedirectAttributes redirectAttributes) {

        try {

            if (quantity < 1) {
                quantity = 1;
            }

            cartService.addToCart(
                    USER_ID,
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
            RedirectAttributes redirectAttributes) {

        try {

            if (quantity < 1) {
                quantity = 1;
            }

            /*
             * Add product to cart first.
             */
            cartService.addToCart(
                    USER_ID,
                    id,
                    quantity
            );

            /*
             * Then directly open checkout page.
             */
            return "redirect:/checkout";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            /*
             * If something goes wrong,
             * return to the product details page.
             */
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
            RedirectAttributes redirectAttributes) {

        try {

            cartService.updateQuantity(
                    USER_ID,
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
            RedirectAttributes redirectAttributes) {

        try {

            savedItemService.saveForLater(
                    USER_ID,
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
            RedirectAttributes redirectAttributes) {

        try {

            cartService.removeItem(
                    USER_ID,
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
            RedirectAttributes redirectAttributes) {

        try {

            cartService.clearCart(USER_ID);

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