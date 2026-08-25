package com.example.mohit.Ecomm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.mohit.Ecomm.model.Orders;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    @Query("SELECT o FROM Orders o JOIN FETCH o.user")
    List<Orders> findAllOrderWithUsers();

}