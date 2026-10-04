package com.example.mohit.Ecomm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.SavedItem;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.model.Product;

public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {

    List<SavedItem> findByUser(User user);

    SavedItem findByUserAndProduct(User user, Product product);
}