package com.example.mohit.Ecomm.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.CartItem;
import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.User;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    void deleteByUser(User user);

    long countByUser(User user);
}