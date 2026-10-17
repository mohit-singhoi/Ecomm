package com.example.mohit.Ecomm.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.CartItem;
import com.example.mohit.Ecomm.model.Orders;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.CartService;
import com.example.mohit.Ecomm.service.OrderService;
import com.example.mohit.Ecomm.service.RazorpayService;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final RazorpayService razorpayService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    public CheckoutController(
            CartService cartService,
            OrderService orderService,
            RazorpayService razorpayService) {

        this.cartService = cartService;
        this.orderService = orderService;
        this.razorpayService = razorpayService;
    }

    // =====================================================
    // SHOW CHECKOUT
    // GET /checkout
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
                    "/checkout");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue to checkout.");

            return "redirect:/userlogin";
        }

        List<CartItem> cartItems =
                cartService.getCartItems(loggedInUser.getId());

        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart is empty.");

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
    // POST /checkout/place-order
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
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/checkout");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to place your order.");

            return "redirect:/userlogin";
        }

        // Validate customer details.
        if (isBlank(fullName)
                || isBlank(email)
                || isBlank(phone)
                || isBlank(address)
                || isBlank(city)
                || isBlank(pinCode)
                || isBlank(payment)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please fill all details before placing the order.");

            return "redirect:/checkout";
        }

        String normalizedPayment =
                payment.trim().toUpperCase();

        if (!normalizedPayment.equals("COD")
                && !normalizedPayment.equals("ONLINE")) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a valid payment method.");

            return "redirect:/checkout";
        }

        Long userId = loggedInUser.getId();

        List<CartItem> cartItems =
                cartService.getCartItems(userId);

        if (cartItems == null || cartItems.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart is empty.");

            return "redirect:/cart";
        }

        // Build product quantities from the server-side cart.
        Map<Long, Integer> productQuantities =
                new LinkedHashMap<>();

        for (CartItem cartItem : cartItems) {

            if (cartItem.getProduct() == null
                    || cartItem.getProduct().getId() == null
                    || cartItem.getQuantity() == null
                    || cartItem.getQuantity() <= 0) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Your cart contains an invalid item.");

                return "redirect:/cart";
            }

            productQuantities.put(
                    cartItem.getProduct().getId(),
                    cartItem.getQuantity());
        }

        try {

            // =============================================
            // CASH ON DELIVERY
            // =============================================

            if ("COD".equals(normalizedPayment)) {

                orderService.placeOrder(
                        userId,
                        productQuantities,
                        0.0);

                // Clear cart only after order creation succeeds.
                cartService.clearCart(userId);

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Your order has been placed successfully!");

                redirectAttributes.addFlashAttribute(
                        "paymentMethod",
                        "Cash on Delivery (COD)");

                return "redirect:/checkout/success";
            }

            // =============================================
            // ONLINE PAYMENT - CREATE SHOPSPHERE ORDER
            // =============================================

            Orders order =
                    orderService.createOnlinePaymentOrder(
                            userId,
                            productQuantities);

            // Create the corresponding Razorpay order.
            JSONObject razorpayOrder =
                    razorpayService.createOrder(
                            order.getTotalAmount(),
                            "SHOPSPHERE_" + order.getId());

            String razorpayOrderId =
                    razorpayOrder.getString("id");

            long amountInPaise =
                    razorpayOrder.getLong("amount");

            String currency =
                    razorpayOrder.getString("currency");

            // Save Razorpay order ID against our database order.
            orderService.saveRazorpayOrderId(
                    order.getId(),
                    userId,
                    razorpayOrderId);

            // Supply all attributes required by payment.html.
            model.addAttribute(
                    "razorpayKeyId",
                    razorpayKeyId);

            model.addAttribute(
                    "razorpayOrderId",
                    razorpayOrderId);

            model.addAttribute(
                    "amountInPaise",
                    amountInPaise);

            model.addAttribute(
                    "currency",
                    currency);

            model.addAttribute(
                    "shopSphereOrderId",
                    order.getId());

            model.addAttribute(
                    "totalAmount",
                    order.getTotalAmount());

            model.addAttribute(
                    "customerName",
                    fullName.trim());

            model.addAttribute(
                    "customerEmail",
                    email.trim());

            model.addAttribute(
                    "customerPhone",
                    phone.trim());

            return "payment";

        } catch (RazorpayException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to initiate online payment. Please try again.");

            return "redirect:/checkout";

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to place your order. Please try again.");

            return "redirect:/checkout";
        }
    }

    // =====================================================
    // VERIFY PAYMENT
    // POST /checkout/verify-payment
    // =====================================================

    @PostMapping("/verify-payment")
    public String verifyPayment(
            @RequestParam Long shopSphereOrderId,
            @RequestParam("razorpay_order_id")
            String razorpayOrderId,
            @RequestParam("razorpay_payment_id")
            String razorpayPaymentId,
            @RequestParam("razorpay_signature")
            String razorpaySignature,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to verify your payment.");

            return "redirect:/userlogin";
        }

        try {

            // 1. Verify the payment signature.
            boolean signatureValid =
                    razorpayService.verifyPaymentSignature(
                            razorpayOrderId,
                            razorpayPaymentId,
                            razorpaySignature);

            if (!signatureValid) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Payment verification failed. Your order has not been marked as paid.");

                return "redirect:/checkout";
            }

            // 2. Retrieve the order belonging to this customer.
            Orders order =
                    orderService.getOrderForCustomer(
                            shopSphereOrderId,
                            loggedInUser.getId());

            // 3. Verify the Razorpay order ID.
            if (order.getRazorpayOrderId() == null
                    || !order.getRazorpayOrderId()
                            .equals(razorpayOrderId)) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The payment order does not match your ShopSphere order.");

                return "redirect:/checkout";
            }

            // 4. Fetch authoritative payment details from Razorpay.
            JSONObject payment =
                    razorpayService.fetchPayment(
                            razorpayPaymentId);

            if (!razorpayPaymentId.equals(
                        payment.optString("id"))
                    || !razorpayOrderId.equals(
                        payment.optString("order_id"))
                    || !"captured".equalsIgnoreCase(
                        payment.optString("status"))
                    || !"INR".equalsIgnoreCase(
                        payment.optString("currency"))) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Payment is not captured or its details do not match.");

                return "redirect:/checkout";
            }

            long verifiedAmountInPaise =
                    payment.getLong("amount");

            // 5. Mark the order paid after all checks pass.
            orderService.markOrderAsPaid(
                    shopSphereOrderId,
                    loggedInUser.getId(),
                    razorpayOrderId,
                    razorpayPaymentId,
                    verifiedAmountInPaise,
                    payment.getString("currency"));

            // 6. Clear cart only after successful verification.
            cartService.clearCart(loggedInUser.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your online payment was successful!");

            redirectAttributes.addFlashAttribute(
                    "paymentMethod",
                    "Online Payment");

            return "redirect:/checkout/success";

        } catch (RazorpayException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to confirm payment with Razorpay. If money was deducted, please contact support.");

            return "redirect:/checkout";

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to verify payment. Please try again or contact support if money was deducted.");

            return "redirect:/checkout";
        }
    }

    // =====================================================
    // ORDER SUCCESS PAGE
    // GET /checkout/success
    // =====================================================

    @GetMapping("/success")
    public String orderSuccess(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login to continue.");

            return "redirect:/userlogin";
        }

        model.addAttribute(
                "customerName",
                loggedInUser.getName());

        return "order-success";
    }

    // =====================================================
    // VALIDATION HELPER
    // =====================================================

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}