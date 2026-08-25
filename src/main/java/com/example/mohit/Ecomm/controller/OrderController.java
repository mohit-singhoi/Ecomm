package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.model.OrderRequest;
import com.example.mohit.Ecomm.service.OrderService;

@RestController
@RequestMapping("/orders")
@CrossOrigin("*")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@PostMapping("/place/{userId}")
	public OrderDTO placeOrder(@PathVariable Long userId, @RequestBody OrderRequest orderrequest) {
		return orderService.placeOrder(userId,orderrequest.getProductQuantities(), orderrequest.getTotalAmount());
	}

}
