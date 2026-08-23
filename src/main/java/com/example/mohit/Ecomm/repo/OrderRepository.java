package com.example.mohit.Ecomm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.Orders;


public interface OrderRepository extends JpaRepository<Orders, Long>{

}
