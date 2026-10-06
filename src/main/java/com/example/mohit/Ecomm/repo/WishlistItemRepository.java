package com.example.mohit.Ecomm.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.model.WishlistItem;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUser(User user);

    Optional<WishlistItem> findByUserAndProduct(
            User user,
            Product product
    );

    long countByUser(User user);

    void deleteByUserAndProduct(
            User user,
            Product product
    );
}