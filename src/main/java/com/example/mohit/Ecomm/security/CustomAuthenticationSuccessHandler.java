package com.example.mohit.Ecomm.security;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;


    public CustomAuthenticationSuccessHandler(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }


    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication)
            throws IOException, ServletException {


        // =====================================================
        // GET LOGGED-IN USER
        // =====================================================

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElse(null);


        if (user == null) {

            response.sendRedirect("/userlogin?error=true");

            return;
        }


        // =====================================================
        // GET AUTHORITIES / ROLES
        // =====================================================

        Set<String> authorities = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());


        boolean isAdmin =
                authorities.contains("ROLE_ADMIN");


        // =====================================================
        // GET LOGIN URL
        // =====================================================

        String requestUri = request.getRequestURI();


        // =====================================================
        // CREATE / GET SESSION
        // =====================================================

        HttpSession session = request.getSession();

        session.setAttribute("loggedInUser", user);


        // =====================================================
        // ADMIN LOGIN
        // =====================================================

        if ("/adminlogin".equals(requestUri)) {


            /*
             * Only ROLE_ADMIN can use the admin login.
             */

            if (!isAdmin) {

                session.invalidate();

                response.sendRedirect(
                    "/adminlogin?error=true"
                );

                return;
            }


            /*
             * Admin authenticated successfully.
             */

            response.sendRedirect("/admin/dashboard");

            return;
        }


        // =====================================================
        // USER LOGIN
        // =====================================================

        /*
         * Admin users are also allowed to use the normal
         * user login and will be taken to the normal dashboard.
         *
         * If you want to restrict admins to admin login only,
         * we can change this later.
         */

        String redirectAfterLogin =
                (String) session.getAttribute(
                    "redirectAfterLogin"
                );


        if (redirectAfterLogin != null
                && !redirectAfterLogin.isBlank()) {

            session.removeAttribute(
                "redirectAfterLogin"
            );

            response.sendRedirect(
                redirectAfterLogin
            );

            return;
        }


        // Normal user destination
        response.sendRedirect("/dashboard");
    }
}