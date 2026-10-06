package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.mohit.Ecomm.repo.ProductRepository;

@Controller
public class SearchController {

    @Autowired
    private ProductRepository productRepository;
    
    

    @GetMapping("/search")
    public String searchPage(
            @RequestParam(required = false) String keyword,
            Model model) {

        if (keyword != null && !keyword.trim().isEmpty()) {

            model.addAttribute(
                    "products",
                    productRepository.findByNameContainingIgnoreCase(keyword)
            );

        } else {

            model.addAttribute(
                    "products",
                    productRepository.findAll()
            );
        }

        model.addAttribute("keyword", keyword);

        return "search";
    }
}