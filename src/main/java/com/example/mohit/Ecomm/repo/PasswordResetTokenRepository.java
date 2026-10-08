package com.example.mohit.Ecomm.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.PasswordResetToken;
import com.example.mohit.Ecomm.model.User;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);
}