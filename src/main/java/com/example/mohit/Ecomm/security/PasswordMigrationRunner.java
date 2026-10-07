package com.example.mohit.Ecomm.security;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;

@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(@NonNull String... args) {

        List<User> users = userRepository.findAll();

        for (User user : users) {

            String password = user.getPassword();

            if (password == null || password.isBlank()) {
                continue;
            }

            if (!isBCryptPassword(password)) {

                user.setPassword(
                    passwordEncoder.encode(password)
                );

                userRepository.save(user);

                System.out.println(
                    "Password migrated to BCrypt for user: "
                    + user.getEmail()
                );
            }
        }
    }

    private boolean isBCryptPassword(String password) {

        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}