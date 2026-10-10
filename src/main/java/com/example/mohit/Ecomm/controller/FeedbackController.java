package com.example.mohit.Ecomm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.example.mohit.Ecomm.model.Feedback;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.FeedbackService;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Display the feedback page.
    @GetMapping
    public String showFeedbackPage(
            HttpSession session,
            Model model) {

        Object loggedInUser = session.getAttribute("loggedInUser");

        // Only logged-in users can access the page.
        if (!(loggedInUser instanceof User)) {
        	return "redirect:/userlogin";
        }

        if (!model.containsAttribute("feedbackRequest")) {
            model.addAttribute("feedbackRequest", new Feedback());
        }

        return "feedback";
    }

    // Process and save submitted feedback.
    @PostMapping("/submit")
    public String submitFeedback(
            @SessionAttribute(name = "loggedInUser", required = false)
            User loggedInUser,

            @Valid
            @ModelAttribute("feedbackRequest")
            Feedback feedback,

            BindingResult bindingResult,

            RedirectAttributes redirectAttributes) {

        // Prevent submissions without a logged-in user.
        if (loggedInUser == null) {
        	return "redirect:/userlogin";
        }

        // Validate all submitted fields.
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.feedbackRequest",
                    bindingResult);

            redirectAttributes.addFlashAttribute(
                    "feedbackRequest", feedback);

            return "redirect:/feedback";
        }

        try {

            // Save feedback for the currently logged-in user.
            feedbackService.saveFeedback(feedback, loggedInUser);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your feedback has been submitted successfully. Thank you for helping us improve ShopSphere!");

            return "redirect:/feedback";

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "We couldn't submit your feedback. Please try again.");

            return "redirect:/feedback";
        }
    }
}