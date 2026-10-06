package com.example.mohit.Ecomm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.ContactMessage;

public interface ContactMessageRepository
        extends JpaRepository<ContactMessage, Long> {

}