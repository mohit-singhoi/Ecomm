package com.example.mohit.Ecomm.controller;

import java.time.temporal.ChronoUnit;
import java.util.Date;
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

import com.example.mohit.Ecomm.model.OrderItem;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.model.ReturnRequest;
import com.example.mohit.Ecomm.service.ReturnRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my-orders/return")
public class ReturnRequestController {

    @Autowired
    private ReturnRequestService returnRequestService;

    private final List<String> returnReasons = List.of(
            "Damaged or defective product",
            "Wrong product delivered",
            "Product differs from description",
            "Size or fit issue",
            "Other"
    );

    // =====================================================
    // DISPLAY RETURN REQUEST FORM
    // =====================================================

    @GetMapping("/{orderItemId}")
    public String showReturnForm(
            @PathVariable Long orderItemId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/my-orders");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in to request a return.");

            return "redirect:/userlogin";
        }

        try {

            OrderItem item =
                    returnRequestService.getEligibleOrderItem(
                            orderItemId,
                            loggedInUser.getId());

            Date deliveredAt =
                    item.getOrder().getDeliveredAt();

            Date returnDeadline = Date.from(
                    deliveredAt.toInstant()
                            .plus(7, ChronoUnit.DAYS));

            model.addAttribute("returnItem", item);

            model.addAttribute(
                    "returnReasons",
                    returnReasons);

            model.addAttribute(
                    "returnDeadline",
                    returnDeadline);

            return "return-request";

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());

            return "redirect:/my-orders";
        }
    }


    // =====================================================
    // SUBMIT RETURN REQUEST
    // =====================================================

    @PostMapping("/{orderItemId}")
    public String submitReturnRequest(
            @PathVariable Long orderItemId,
            @RequestParam int returnQuantity,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in to request a return.");

            return "redirect:/userlogin";
        }

        try {

            ReturnRequest request =
                    returnRequestService.createReturnRequest(
                            orderItemId,
                            loggedInUser.getId(),
                            returnQuantity,
                            reason,
                            description);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Return request #" + request.getId()
                            + " submitted successfully. "
                            + "Your request is awaiting review.");

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return "redirect:/my-orders";
    }
}