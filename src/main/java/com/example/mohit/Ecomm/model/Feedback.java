package com.example.mohit.Ecomm.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "feedback")
public class Feedback {

    // Feedback status
    public enum FeedbackStatus {
        PENDING,
        IN_PROGRESS,
        RESOLVED
    }

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Feedback category
    @NotBlank(message = "Please select a feedback category.")
    @Column(nullable = false, length = 100)
    private String category;

    // Customer rating
    @NotNull(message = "Please select a rating.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot exceed 5.")
    @Column(nullable = false)
    private Integer rating;

    // Feedback subject
    @NotBlank(message = "Please enter a subject.")
    @Size(
        min = 3,
        max = 150,
        message = "Subject must contain 3 to 150 characters."
    )
    @Column(nullable = false, length = 150)
    private String subject;

    // Original customer message
    @NotBlank(message = "Please enter your feedback.")
    @Size(
        min = 10,
        max = 2000,
        message = "Feedback must contain 10 to 2000 characters."
    )
    @Column(nullable = false, length = 2000)
    private String message;

    // Customer who submitted the feedback
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Feedback submission date
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Current feedback status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.PENDING;

    // Administrator's response
    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    // Date and time when the administrator responded
    private LocalDateTime respondedAt;

    // Initialize fields before inserting a new record
    @PrePersist
    public void beforeSave() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = FeedbackStatus.PENDING;
        }
    }

    // Default constructor
    public Feedback() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public FeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(FeedbackStatus status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}