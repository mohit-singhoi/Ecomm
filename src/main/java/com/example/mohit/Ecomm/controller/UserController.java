package com.example.mohit.Ecomm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin("*")
public class UserController {


@Autowired
private UserService userService;

// Safe response DTO: never exposes password.
public record UserResponse(
        Long id,
        String name,
        String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}

// Register User
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody User user) {

    try {
        User registeredUser = userService.registerUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(registeredUser));

    } catch (IllegalArgumentException e) {

        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}

// Login User
@PostMapping("/login")
public ResponseEntity<?> loginUser(@RequestBody User user) {

    User loggedInUser = userService.loginUser(
            user.getEmail(),
            user.getPassword()
    );

    if (loggedInUser == null) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password.");
    }

    return ResponseEntity.ok(
            UserResponse.from(loggedInUser)
    );
}

// Get All Users
@GetMapping
public List<UserResponse> getAllUsers() {

    return userService.getAllUsers()
            .stream()
            .map(UserResponse::from)
            .toList();
}


}
