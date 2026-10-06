package com.example.mohit.Ecomm.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mohit.Ecomm.model.ContactMessage;
import com.example.mohit.Ecomm.repo.ContactMessageRepository;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;


    public ContactMessage saveMessage(
            String name,
            String email,
            String subject,
            String message) {

        ContactMessage contactMessage = new ContactMessage();

        contactMessage.setName(name);
        contactMessage.setEmail(email);
        contactMessage.setSubject(subject);
        contactMessage.setMessage(message);
        contactMessage.setCreatedAt(LocalDateTime.now());

        return contactMessageRepository.save(contactMessage);
    }
}