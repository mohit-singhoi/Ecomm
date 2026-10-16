package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.service.ProductService;
import com.example.mohit.Ecomm.service.ProductImageService;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    // ==============================
    // SHOW ALL PRODUCTS OR FILTER BY CATEGORY
    // ==============================
    @GetMapping
    public String getAllProducts(
            @RequestParam(required = false) String category,
            Model model) {

        List<Product> products;

        if (category != null && !category.isBlank()) {
            products = productService.getActiveProductsByCategory(
                    category.trim()
            );
        } else {
            products = productService.getActiveProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);

        return "products";
    }

    // ==============================
    // SHOW PRODUCTS BY CATEGORY
    // ==============================
    @GetMapping("/category/{category}")
    public String getProductsByCategory(
            @PathVariable String category,
            Model model) {

        List<Product> products =
                productService.getActiveProductsByCategory(category.trim());

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);

        return "products";
    }

    // ==============================
    // SHOW PRODUCT DETAILS WITH IMAGE GALLERY
    // ==============================
    @GetMapping("/{id}")
    public String productDetails(
            @PathVariable Long id,
            Model model) {

        Product product = productService.getProductById(id);

        if (product == null || !product.isActive()) {
            return "redirect:/products";
        }

        // Existing product information
        model.addAttribute("product", product);

        // Load all uploaded images for this product
        model.addAttribute(
                "productImages",
                productImageService.getProductImages(id)
        );

        return "product-details";
    }
}