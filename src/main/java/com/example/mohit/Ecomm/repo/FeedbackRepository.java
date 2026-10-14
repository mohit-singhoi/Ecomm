package com.example.mohit.Ecomm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mohit.Ecomm.model.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Find feedback submitted by a particular customer
    List<Feedback> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Retrieve all feedback, newest first
    List<Feedback> findAllByOrderByCreatedAtDesc();

    // Count feedback by status
    long countByStatus(Feedback.FeedbackStatus status);

    // Retrieve feedback filtered by status
    List<Feedback> findByStatusOrderByCreatedAtDesc(
            Feedback.FeedbackStatus status
    );
}