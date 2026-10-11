package com.example.mohit.Ecomm.security;

import java.io.IOException;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        // Check the exception and its underlying causes.
        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof DisabledException) {
                response.sendRedirect(
                    request.getContextPath()
                    + "/userlogin?error=deactivated"
                );
                return;
            }

            cause = cause.getCause();
        }

        // Incorrect credentials or another authentication failure.
        response.sendRedirect(
            request.getContextPath()
            + "/userlogin?error=invalid"
        );
    }
}