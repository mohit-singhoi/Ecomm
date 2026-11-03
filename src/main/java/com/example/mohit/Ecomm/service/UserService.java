package com.example.mohit.Ecomm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register User Method 
    
    public User registerUser(User user) {

        if (user == null
                || user.getPassword() == null
                || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        // Never allow public registration to create an admin.
        user.setRole("USER");

        // New accounts are active by default.
        user.setActive(true);

        // Encode password using BCrypt.
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    // Login User Method 
    
    public User loginUser(String email, String password) {

        if (email == null || password == null) {
            return null;
        }

        Optional<User> userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isPresent()) {

            User user = userOptional.get();

            if (!user.isActive()) {
                return null;
            }

            if (user.getPassword() != null
                    && passwordEncoder.matches(
                            password,
                            user.getPassword())) {

                return user;
            }
        }

        return null;
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }
}