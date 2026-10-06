package com.example.mohit.Ecomm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByNameContainingIgnoreCase(String name);
}