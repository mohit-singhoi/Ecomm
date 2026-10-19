package com.example.mohit.Ecomm.dto;

public class OrderItemDTO {

    private Long id;
    private String productName;
    private double productPrice;
    private int quantity;

    // Default constructor
    public OrderItemDTO() {
    }

    // Existing constructor — preserved for compatibility
    public OrderItemDTO(
            String productName,
            double productPrice,
            int quantity) {

        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
    }

    // New constructor with order item ID
    public OrderItemDTO(
            Long id,
            String productName,
            double productPrice,
            int quantity) {

        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}