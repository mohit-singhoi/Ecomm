package com.example.mohit.Ecomm.controller;

import java.util.List;

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
import com.example.mohit.Ecomm.service.FeedbackService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
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

            return "admin/feedback-details";

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
            feedbackService.saveAdminResponse(id, adminResponse);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your response has been saved successfully."
            );

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