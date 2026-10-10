package com.example.mohit.Ecomm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.OrderItemRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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

    // Check whether a product is associated with an order
    public boolean isProductUsedInOrders(Long productId) {
        return orderItemRepository.existsByProduct_Id(productId);
    }

    /*
     * Delete a product safely.
     *
     * If an order references the product, deactivate it.
     * Otherwise, permanently delete it.
     */
    @Transactional
    public String deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElse(null);

        if (product == null) {
            return "NOT_FOUND";
        }

        boolean usedInOrders =
                orderItemRepository.existsByProduct_Id(id);

        if (usedInOrders) {

            product.setActive(false);

            productRepository.save(product);

            return "DEACTIVATED";
        }

        productRepository.delete(product);

        return "DELETED";
    }
    
 // Get active products
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    // Get active products by category
    public List<Product> getActiveProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
    }

    // Search active products by name
    public List<Product> searchActiveProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }
}