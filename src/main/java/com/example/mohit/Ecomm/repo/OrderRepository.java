package com.example.mohit.Ecomm.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mohit.Ecomm.model.Orders;
import com.example.mohit.Ecomm.model.User;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    // Get all orders with customer details
    @Query("SELECT o FROM Orders o JOIN FETCH o.user")
    List<Orders> findAllOrderWithUsers();

    // Get orders belonging to a particular customer
    List<Orders> findByUser(User user);

    // Calculate total sales
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Orders o")
    Double getTotalSales();

    // Find an order using its Razorpay order ID
    Optional<Orders> findByRazorpayOrderId(String razorpayOrderId);

    // Find an order using its Razorpay payment ID
    Optional<Orders> findByRazorpayPaymentId(String razorpayPaymentId);

    // Find orders by payment status
    List<Orders> findByPaymentStatus(String paymentStatus);

    // Find a customer's orders by payment method
    List<Orders> findByUserAndPaymentMethod(User user, String paymentMethod);

    // Find orders awaiting payment
    List<Orders> findByPaymentStatusAndPaymentMethod(
            String paymentStatus,
            String paymentMethod
    );

    // Find an order belonging to a particular customer
    // using the order ID and customer ID
    @Query("""
        SELECT o FROM Orders o
        WHERE o.id = :orderId
        AND o.user.id = :userId
        """)
    Optional<Orders> findOrderByIdAndUserId(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );
}