package com.example.mohit.Ecomm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{

}
