package com.example.mohit.Ecomm.controller;

import org.springframework.mail.MailException;
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
import com.example.mohit.Ecomm.service.EmailService;
import com.example.mohit.Ecomm.service.FeedbackService;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

	private final FeedbackService feedbackService;
	private final EmailService emailService;

	public FeedbackController(
	        FeedbackService feedbackService,
	        EmailService emailService) {

	    this.feedbackService = feedbackService;
	    this.emailService = emailService;
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

        // Ensure the customer is logged in.
        if (loggedInUser == null) {
            return "redirect:/userlogin";
        }

        // Validate the submitted feedback.
        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.feedbackRequest",
                    bindingResult);

            redirectAttributes.addFlashAttribute(
                    "feedbackRequest",
                    feedback);

            return "redirect:/feedback";
        }

        try {

            // Step 1: Save feedback in the database.
            feedbackService.saveFeedback(feedback, loggedInUser);

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "We couldn't submit your feedback. Please try again.");

            return "redirect:/feedback";
        }

        // Step 2: Send the acknowledgement email separately.
        try {

            emailService.sendFeedbackAcknowledgement(
                    loggedInUser.getEmail(),
                    loggedInUser.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your feedback has been submitted successfully. "
                    + "A confirmation email has been sent to you.");

        } catch (MailException | IllegalArgumentException exception) {

            // Feedback is already saved, even if email delivery fails.
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your feedback has been submitted successfully.");

            redirectAttributes.addFlashAttribute(
                    "emailWarning",
                    "We couldn't send the confirmation email. "
                    + "Your feedback has still been recorded.");
        }

        return "redirect:/feedback";
    }
}