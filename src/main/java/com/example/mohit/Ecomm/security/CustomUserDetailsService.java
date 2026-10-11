package com.example.mohit.Ecomm.security;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(
            @NonNull String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "Invalid email or password."
                    )
                );

        // Reject deactivated accounts.
        if (!user.isActive()) {
            throw new org.springframework.security.authentication.DisabledException(
                "Your account has been deactivated. Please contact the administration team for assistance."
            );
        }

        // Get the user's role.
        String role = user.getRole();

        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        }

        // Spring Security's roles() method adds ROLE_ automatically.
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        // Return Spring Security's UserDetails object.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(role)
                .build();
    }
}