package com.example.mohit.Ecomm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mohit.Ecomm.model.Feedback;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public Feedback saveFeedback(Feedback feedback, User user) {

        // Associate feedback with the authenticated session user.
        feedback.setUser(user);

        // Do not accept an ID or creation date supplied by the form.
        feedback.setId(null);
        feedback.setCreatedAt(null);

        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackByUser(Long userId) {
        return feedbackRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }
}