package com.example.mohit.Ecomm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mohit.Ecomm.model.OrderItem;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    boolean existsByProduct_Id(Long productId);
}