package com.example.mohit.Ecomm.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "return_requests")
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer who submitted the return request
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Specific product line being returned
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    // Quantity the customer wants to return
    @Column(nullable = false)
    private int returnQuantity;

    // Reason selected by the customer
    @Column(nullable = false, length = 100)
    private String reason;

    // Additional explanation
    @Column(length = 1000)
    private String description;

    // REQUESTED, APPROVED, REJECTED, etc.
    @Column(nullable = false, length = 40)
    private String status = "REQUESTED";

    // Administrator's decision or explanation
    @Column(length = 1000)
    private String adminRemarks;

    // Calculated by the backend after the request is reviewed
    @Column
    private Double refundAmount;

    // NOT_APPLICABLE, PENDING, INITIATED, REFUNDED, FAILED
    @Column(length = 40)
    private String refundStatus = "NOT_APPLICABLE";
    
 // Uploaded product evidence image filename
    @Column(name = "evidence_image_path", length = 255)
    private String evidenceImagePath;

    // Created timestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date requestDate;

    // Last updated timestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    // Timestamp of the admin's decision
    @Temporal(TemporalType.TIMESTAMP)
    private Date decisionDate;

    // Razorpay refund reference, if applicable
    @Column(length = 100)
    private String razorpayRefundId;
    
 // =====================================================
 // COD MANUAL REFUND DETAILS
 // =====================================================

 // UPI, BANK_TRANSFER, or another supported manual method
 @Column(name = "cod_refund_method", length = 40)
 private String codRefundMethod;

 // UTR number or payment transaction reference
 @Column(name = "cod_refund_reference", length = 150)
 private String codRefundReference;

 // Date the manual refund was actually sent
 @Temporal(TemporalType.TIMESTAMP)
 @Column(name = "cod_refund_date")
 private Date codRefundDate;

 // Tracks whether the COD refund was recorded by the admin
 @Column(name = "cod_refund_recorded", nullable = false)
 private boolean codRefundRecorded = false;
 
    
 // Tracks whether the successful refund email has been sent
    @Column(name = "refund_email_sent", nullable = false)
    private boolean refundEmailSent = false;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();

        if (requestDate == null) {
            requestDate = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public int getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(int returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Date decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getRazorpayRefundId() {
        return razorpayRefundId;
    }

    public void setRazorpayRefundId(String razorpayRefundId) {
        this.razorpayRefundId = razorpayRefundId;
    }
    
    public String getEvidenceImagePath() {
        return evidenceImagePath;
    }

    public void setEvidenceImagePath(String evidenceImagePath) {
        this.evidenceImagePath = evidenceImagePath;
    }
    
    public boolean isRefundEmailSent() {
        return refundEmailSent;
    }

    public void setRefundEmailSent(boolean refundEmailSent) {
        this.refundEmailSent = refundEmailSent;
    }
    
 // =====================================================
 // COD REFUND GETTERS AND SETTERS
 // =====================================================

 public String getCodRefundMethod() {
     return codRefundMethod;
 }

 public void setCodRefundMethod(String codRefundMethod) {
     this.codRefundMethod = codRefundMethod;
 }

 public String getCodRefundReference() {
     return codRefundReference;
 }

 public void setCodRefundReference(String codRefundReference) {
     this.codRefundReference = codRefundReference;
 }

 public Date getCodRefundDate() {
     return codRefundDate;
 }

 public void setCodRefundDate(Date codRefundDate) {
     this.codRefundDate = codRefundDate;
 }

 public boolean isCodRefundRecorded() {
     return codRefundRecorded;
 }

 public void setCodRefundRecorded(boolean codRefundRecorded) {
     this.codRefundRecorded = codRefundRecorded;
 }
}