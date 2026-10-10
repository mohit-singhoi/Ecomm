package com.example.mohit.Ecomm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    // Get all active products available in the store
    List<Product> findByActiveTrue();

    // Get active products by category
    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);

    // Search active products by name
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Existing methods — retained for admin functionality
    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByNameContainingIgnoreCase(String name);
    
    
}