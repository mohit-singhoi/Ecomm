//Product Controller 

package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ==============================
    // SHOW ALL PRODUCTS
    // ==============================
    @SuppressWarnings("null")
	@GetMapping
    public String getAllProducts(Model model) {

        List<Product> products = productService.getAllProducts();

        model.addAttribute("products", products);

        return "products";
    }

 // ==============================
 // SHOW PRODUCT DETAILS
 // ==============================

 @GetMapping("/{id}")
 public String productDetails(@PathVariable Long id, Model model) {

     Product product = productService.getProductById(id);

     model.addAttribute("product", product);

     return "product-details";
 }
}