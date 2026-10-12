package com.example.mohit.Ecomm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.dto.OrderItemDTO;
import com.example.mohit.Ecomm.model.OrderItem;
import com.example.mohit.Ecomm.model.Orders;
import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.OrderRepository;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

 // =====================================================
 // PLACE ORDER
 // =====================================================

 @Transactional
 public OrderDTO placeOrder(
         Long userId,
         Map<Long, Integer> productQuantities,
         double totalAmount) {

     User user = userRepository.findById(userId)
             .orElseThrow(() ->
                     new RuntimeException("User not found."));

     Orders order = new Orders();
     order.setUser(user);
     order.setOrderDate(new Date());
     order.setStatus("Pending");

     List<OrderItem> orderItems = new ArrayList<>();
     List<OrderItemDTO> orderItemDTOs = new ArrayList<>();

     // Calculate the order total using database prices
     double calculatedTotal = 0.0;

     for (Map.Entry<Long, Integer> entry
             : productQuantities.entrySet()) {

         Long productId = entry.getKey();
         Integer quantity = entry.getValue();

         // Validate quantity
         if (quantity == null || quantity <= 0) {
             throw new RuntimeException(
                     "Product quantity must be greater than zero.");
         }

         // Fetch the current product from the database
         Product product = productRepository.findById(productId)
                 .orElseThrow(() ->
                         new RuntimeException("Product not found."));

         // Calculate the actual item total
         double itemTotal = product.getPrice() * quantity;
         calculatedTotal += itemTotal;

         // Create order item
         OrderItem orderItem = new OrderItem();
         orderItem.setOrder(order);
         orderItem.setProduct(product);
         orderItem.setQuantity(quantity);

         orderItems.add(orderItem);

         // Prepare item details for the response
         orderItemDTOs.add(
                 new OrderItemDTO(
                         product.getName(),
                         product.getPrice(),
                         quantity
                 )
         );
     }

     // Save the calculated total, not the frontend-provided amount
     order.setTotalAmount(calculatedTotal);
     order.setOrderItems(orderItems);

     // Save order and items
     Orders savedOrder = orderRepository.save(order);

     return new OrderDTO(
             savedOrder.getId(),
             savedOrder.getTotalAmount(),
             savedOrder.getStatus(),
             savedOrder.getOrderDate(),
             orderItemDTOs
     );
 }
    // =====================================================
    // GET ALL ORDERS
    // =====================================================

    public List<OrderDTO> getAllOrders() {

        List<Orders> orders =
                orderRepository.findAllOrderWithUsers();

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // CONVERT ORDER ENTITY TO DTO
    // =====================================================

    private OrderDTO convertToDTO(Orders order) {

        List<OrderItemDTO> orderItems =
                order.getOrderItems()
                        .stream()
                        .map(item -> new OrderItemDTO(
                                item.getProduct().getName(),
                                item.getProduct().getPrice(),
                                item.getQuantity()
                        ))
                        .collect(Collectors.toList());

        return new OrderDTO(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                order.getUser() != null
                        ? order.getUser().getName()
                        : "Unknown",
                order.getUser() != null
                        ? order.getUser().getEmail()
                        : "Unknown",
                orderItems
        );
    }

    // =====================================================
    // GET ORDERS BY USER
    // =====================================================

    public List<OrderDTO> getOrderByUser(Long userId) {

        Optional<User> userOptional =
                userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        User user = userOptional.get();

        List<Orders> orders =
                orderRepository.findByUser(user);

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // CANCEL ORDER
    // Only the owner can cancel their Pending/Processing order
    // =====================================================

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {

        // Find the order
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found."));

        // Verify order ownership
        if (order.getUser() == null
                || !userId.equals(order.getUser().getId())) {

            throw new RuntimeException(
                    "You are not authorized to cancel this order.");
        }

        // Check whether cancellation is allowed
        String status = order.getStatus();

        if (!"Pending".equals(status)
                && !"Processing".equals(status)) {

            throw new RuntimeException(
                    "This order cannot be cancelled because its current status is "
                            + status + ".");
        }

        // Update order status
        order.setStatus("Cancelled");

        // Save the updated order
        orderRepository.save(order);
    }
}