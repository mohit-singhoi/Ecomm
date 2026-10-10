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

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ==============================
    // SHOW ALL PRODUCTS OR FILTER BY CATEGORY
    // ==============================
    @GetMapping
    public String getAllProducts(
            @RequestParam(required = false) String category,
            Model model) {

        List<Product> products;

        if (category != null && !category.isBlank()) {

            // Show products belonging to the selected category
            products = productService.getActiveProductsByCategory(
                    category.trim()
            );

        } else {

            // Show all active products
            products = productService.getActiveProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);

        return "products";
    }

    // ==============================
    // SHOW PRODUCTS BY CATEGORY PATH
    // Existing route retained
    // ==============================
    @GetMapping("/category/{category}")
    public String getProductsByCategory(
            @PathVariable String category,
            Model model) {

        List<Product> products =
                productService.getActiveProductsByCategory(category);

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);

        return "products";
    }

    // ==============================
    // SHOW PRODUCT DETAILS
    // ==============================
    @GetMapping("/{id}")
    public String productDetails(
            @PathVariable Long id,
            Model model) {

        Product product = productService.getProductById(id);

        if (product == null || !product.isActive()) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);

        return "product-details";
    }
}