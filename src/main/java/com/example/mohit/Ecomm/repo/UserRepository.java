package com.example.mohit.Ecomm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);

}