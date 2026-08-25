package com.example.mohit.Ecomm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.mohit.Ecomm.dto.OrderDTO;
import com.example.mohit.Ecomm.dto.OrderItemDTO;
import com.example.mohit.Ecomm.model.OrderItem;
import com.example.mohit.Ecomm.model.Orders;
import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.OrderRepository;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

public class OrderService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	public OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities, double totalAmount) {
		// TODO Auto-generated method stub
		User user = userRepository.findById(userId)
				  .orElseThrow(()-> new RuntimeException("user Not Found"));
		
		Orders order = new Orders();
		order.setUser(user);
		order.setOrderDate(new Date());
		order.setStatus("Pending");
		order.setTotalAmount(totalAmount);
		
		List<OrderItem> orderItems = new ArrayList<>();
		List<OrderItemDTO> orderItemDTOS = new ArrayList<>();
		
		for(Map.Entry<Long, Integer> entry:productQuantities.entrySet()) {
			
			Product product = productRepository.findById(entry.getKey())
					.orElseThrow(()->new RuntimeException("Product Not Found"));
			
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProduct(product);
			orderItem.setQuantity(entry.getValue());
			orderItems.add(orderItem);
			
			orderItemDTOS.add(new OrderItemDTO(product.getName(),product.getPrice(), entry.getValue()));
			
		}
		order.setOrderItems(orderItems);
		Orders saveOrder = orderRepository.save(order);
		return new OrderDTO(saveOrder.getId(), saveOrder.getTotalAmount(), saveOrder.getStatus(), saveOrder.getOrderDate(),orderItemDTOS);
	}
	

     public List<OrderDTO> getAllOrders(){
     List<Orders> orders =	 orderRepository.findAllOrderWithUsers();
     return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    	 
     }
     
     private OrderDTO convertToDTO(Orders orders) {

    	    List<OrderItemDTO> orderItems = orders.getOrderItems()
    	            .stream()
    	            .map(item -> new OrderItemDTO(
    	                    item.getProduct().getName(),
    	                    item.getProduct().getPrice(),
    	                    item.getQuantity()
    	            ))
    	            .collect(Collectors.toList());

    	    return new OrderDTO(
    	            orders.getId(),
    	            orders.getTotalAmount(),
    	            orders.getStatus(),
    	            orders.getOrderDate(),
    	            orders.getUser() != null ? orders.getUser().getName() : "Unknown",
    	            orders.getUser() != null ? orders.getUser().getEmail() : "Unknown",
    	            orderItems
    	    );
    	}
}
	


