package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.Feedback;
import com.example.mohit.Ecomm.model.Feedback.FeedbackStatus;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.EmailService;
import com.example.mohit.Ecomm.service.FeedbackService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

	private final FeedbackService feedbackService;
	private final EmailService emailService;

	public AdminFeedbackController(
	        FeedbackService feedbackService,
	        EmailService emailService) {

	    this.feedbackService = feedbackService;
	    this.emailService = emailService;
	}

    // ==========================================
    // ADMIN AUTHORIZATION
    // ==========================================

    private boolean isAdmin(HttpSession session) {

        Object loggedInUser = session.getAttribute("loggedInUser");

        return loggedInUser instanceof User user
                && "ROLE_ADMIN".equals(user.getRole());
    }

    // ==========================================
    // VIEW ALL FEEDBACK
    // ==========================================

    @GetMapping
    public String viewAllFeedback(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator to access feedback management."
            );

            return "redirect:/adminlogin";
        }

        List<Feedback> feedbackList;

        FeedbackStatus selectedStatus = null;

        if (status != null && !status.isBlank()) {
            try {
                selectedStatus = FeedbackStatus.valueOf(
                        status.trim().toUpperCase()
                );

                feedbackList = feedbackService.getFeedbackByStatus(
                        selectedStatus
                );

            } catch (IllegalArgumentException exception) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Invalid feedback status selected."
                );

                return "redirect:/admin/feedback";
            }
        } else {
            feedbackList = feedbackService.getAllFeedback();
        }

        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("totalFeedback",
                feedbackService.countAllFeedback());
        model.addAttribute("pendingFeedback",
                feedbackService.countFeedbackByStatus(
                        FeedbackStatus.PENDING));
        model.addAttribute("inProgressFeedback",
                feedbackService.countFeedbackByStatus(
                        FeedbackStatus.IN_PROGRESS));
        model.addAttribute("resolvedFeedback",
                feedbackService.countFeedbackByStatus(
                        FeedbackStatus.RESOLVED));

        model.addAttribute("admin", session.getAttribute("loggedInUser"));
        model.addAttribute("activePage", "feedback");

        return "admin/feedback";
    }

    // ==========================================
    // VIEW INDIVIDUAL FEEDBACK
    // ==========================================

    @GetMapping("/{id}")
    public String viewFeedbackDetails(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required."
            );

            return "redirect:/adminlogin";
        }

        try {
            Feedback feedback = feedbackService.getFeedbackById(id);

            model.addAttribute("feedback", feedback);
            model.addAttribute("admin",
                    session.getAttribute("loggedInUser"));
            model.addAttribute("activePage", "feedback");

            return "admin/view-feedback";

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "The requested feedback could not be found."
            );

            return "redirect:/admin/feedback";
        }
    }

    // ==========================================
    // SAVE ADMIN RESPONSE
    // ==========================================

    @SuppressWarnings("null")
	@PostMapping("/{id}/respond")
    public String respondToFeedback(
            @PathVariable Long id,
            @RequestParam String adminResponse,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required."
            );

            return "redirect:/adminlogin";
        }

        try {
            // 1. Save the admin response.
            Feedback savedFeedback =
                    feedbackService.saveAdminResponse(id, adminResponse);

            // 2. Retrieve customer details safely.
            Feedback feedback =
                    feedbackService.getFeedbackWithCustomer(id);

            String customerEmail = feedback.getUser().getEmail();
            String customerName = feedback.getUser().getName();
            String savedResponse = savedFeedback.getAdminResponse();

            // 3. Send a separate email containing the admin response.
            try {
                emailService.sendFeedbackResponse(
                        customerEmail,
                        customerName,
                        savedResponse
                );

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Response saved and email sent to the customer successfully."
                );

            } catch (MailException | IllegalArgumentException emailException) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The response was saved, but the email could not be sent. "
                        + "Please check your email configuration."
                );
            }

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/feedback/" + id;
    }

    // ==========================================
    // UPDATE FEEDBACK STATUS
    // ==========================================

    @PostMapping("/{id}/status")
    public String updateFeedbackStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required."
            );

            return "redirect:/adminlogin";
        }

        try {
            FeedbackStatus feedbackStatus = FeedbackStatus.valueOf(
                    status.trim().toUpperCase()
            );

            feedbackService.updateFeedbackStatus(id, feedbackStatus);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Feedback status updated successfully."
            );

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to update feedback. Check the selected status and feedback ID."
            );
        }

        return "redirect:/admin/feedback/" + id;
    }
}