package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.service.ProductService;

@RestController
@RequestMapping("/products")
@CrossOrigin("*")
public class ProductController {
	
	@Autowired
	ProductService productService;
	
	@GetMapping
	List<Product> getAllProducts(){
		return productService.getAllProducts();
		
	}
	
	@GetMapping("/{id")
	public Product getProductById(@PathVariable Long id) {
		return productService.getProductById(id);
		
	}
	
	@PostMapping
	public Product addProduct(@RequestBody Product product) {
		return productService.addProduct(product);
	}
	
	
	@DeleteMapping("/{id")
	public void deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
	}
	

}
