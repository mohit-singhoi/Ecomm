package com.example.mohit.Ecomm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mohit.Ecomm.model.Feedback;
import com.example.mohit.Ecomm.model.Feedback.FeedbackStatus;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    // ==========================================
    // CUSTOMER FEEDBACK OPERATIONS
    // ==========================================

    /**
     * Save feedback submitted by the authenticated customer.
     */
    @Transactional
    public Feedback saveFeedback(Feedback feedback, User user) {

        // Associate feedback with the authenticated session user.
        feedback.setUser(user);

        // Do not accept an ID or creation date supplied by the form.
        feedback.setId(null);
        feedback.setCreatedAt(null);

        // Every new feedback submission starts as pending.
        feedback.setStatus(FeedbackStatus.PENDING);

        // Customers cannot submit an administrator response.
        feedback.setAdminResponse(null);
        feedback.setRespondedAt(null);

        return feedbackRepository.save(feedback);
    }

    /**
     * Retrieve all feedback, newest first.
     */
    @Transactional(readOnly = true)
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieve feedback submitted by a particular customer.
     */
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackByUser(Long userId) {
        return feedbackRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    // ==========================================
    // ADMIN FEEDBACK OPERATIONS
    // ==========================================

    /**
     * Retrieve an individual feedback record.
     */
    @Transactional(readOnly = true)
    public Feedback getFeedbackById(Long feedbackId) {

        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found with ID: " + feedbackId
                        )
                );
    }

    /**
     * Save the administrator's response.
     */
    @Transactional
    public Feedback saveAdminResponse(
            Long feedbackId,
            String adminResponse) {

        if (adminResponse == null || adminResponse.isBlank()) {
            throw new IllegalArgumentException(
                    "Admin response cannot be empty."
            );
        }

        String trimmedResponse = adminResponse.trim();

        if (trimmedResponse.length() > 5000) {
            throw new IllegalArgumentException(
                    "Admin response cannot exceed 5000 characters."
            );
        }

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found with ID: " + feedbackId
                        )
                );

        feedback.setAdminResponse(trimmedResponse);
        feedback.setRespondedAt(LocalDateTime.now());

        // A newly submitted response moves the query to In Progress.
        if (feedback.getStatus() == FeedbackStatus.PENDING) {
            feedback.setStatus(FeedbackStatus.IN_PROGRESS);
        }

        return feedbackRepository.save(feedback);
    }

    /**
     * Update the status of a feedback record.
     */
    @Transactional
    public Feedback updateFeedbackStatus(
            Long feedbackId,
            FeedbackStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Please select a valid feedback status."
            );
        }

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found with ID: " + feedbackId
                        )
                );

        feedback.setStatus(status);

        return feedbackRepository.save(feedback);
    }

    /**
     * Count all feedback records.
     */
    @Transactional(readOnly = true)
    public long countAllFeedback() {
        return feedbackRepository.count();
    }

    /**
     * Count feedback records by status.
     */
    @Transactional(readOnly = true)
    public long countFeedbackByStatus(FeedbackStatus status) {

        return feedbackRepository.countByStatus(status);
    }

    /**
     * Retrieve feedback filtered by status.
     */
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackByStatus(FeedbackStatus status) {

        if (status == null) {
            return getAllFeedback();
        }

        return feedbackRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Retrieve feedback with its associated customer.
     * The customer details are accessed inside the transaction.
     */
    @Transactional(readOnly = true)
    public Feedback getFeedbackWithCustomer(Long feedbackId) {

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found with ID: " + feedbackId
                        )
                );

        // Initialize the associated customer while the transaction is open.
        feedback.getUser().getEmail();
        feedback.getUser().getName();

        return feedback;
    }
}