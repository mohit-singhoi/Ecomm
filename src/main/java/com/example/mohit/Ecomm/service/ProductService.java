package com.example.mohit.Ecomm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.repo.ProductRepository;

@Service
public class ProductService {
	
	@Autowired
	ProductRepository productRepository;

	
	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub
		return productRepository.findAll();
	}
	
	
	public Product getProductById(Long id) {
		return productRepository.findById(id).orElse(null);
		
	}
	
	
	public Product addProduct(@RequestBody Product product) {
		return productRepository.save(product);
	}

	
	public void deleteProduct( Long id) {
		// TODO Auto-generated method stub
		productRepository.deleteById(id);
		
		
	}

	
	
	

}
