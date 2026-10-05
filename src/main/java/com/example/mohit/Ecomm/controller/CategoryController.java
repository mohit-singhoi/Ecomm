package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.service.ProductService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showCategories(Model model) {

        List<Product> products = productService.getAllProducts();

        model.addAttribute("products", products);

        return "categories";
    }
}