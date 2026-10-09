package com.example.mohit.Ecomm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.repo.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // Get products by category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    // Search products by name
    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

 // Add product — retained for existing REST API
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Add or update product — used by admin panel
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    

    // Delete product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}