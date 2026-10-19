package com.example.mohit.Ecomm.dto;

import java.util.Date;
import java.util.List;

public class OrderDTO {

    private Long id;
    private double totalAmount;
    private String status;
    private Date orderDate;
    private Date deliveredAt;
    private String userName;
    private String email;
    private List<OrderItemDTO> orderItems;

    // Constructor with customer information
    public OrderDTO(
            Long id,
            double totalAmount,
            String status,
            Date orderDate,
            String userName,
            String email,
            List<OrderItemDTO> orderItems) {

        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.userName = userName;
        this.email = email;
        this.orderItems = orderItems;
    }

    // Constructor with customer information and delivery timestamp
    public OrderDTO(
            Long id,
            double totalAmount,
            String status,
            Date orderDate,
            Date deliveredAt,
            String userName,
            String email,
            List<OrderItemDTO> orderItems) {

        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveredAt = deliveredAt;
        this.userName = userName;
        this.email = email;
        this.orderItems = orderItems;
    }

    // Constructor without customer information
    public OrderDTO(
            Long id,
            double totalAmount,
            String status,
            Date orderDate,
            List<OrderItemDTO> orderItems) {

        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.orderItems = orderItems;
    }

    // Constructor without customer information but with delivery timestamp
    public OrderDTO(
            Long id,
            double totalAmount,
            String status,
            Date orderDate,
            Date deliveredAt,
            List<OrderItemDTO> orderItems) {

        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveredAt = deliveredAt;
        this.orderItems = orderItems;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    // Delivery timestamp getter and setter

    public Date getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Date deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
}