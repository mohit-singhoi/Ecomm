package com.example.mohit.Ecomm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // ADMIN SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(
            HttpSecurity http,
            CustomAuthenticationSuccessHandler successHandler)
            throws Exception {

        http
            /*
             * This security chain handles only admin URLs.
             */
            .securityMatcher(
                "/admin/**",
                "/adminlogin"
            )


            // =================================================
            // AUTHORIZATION
            // =================================================

            .authorizeHttpRequests(auth -> auth

                /*
                 * Admin login page must be publicly accessible.
                 */
                .requestMatchers("/adminlogin")
                .permitAll()

                /*
                 * Every other URL handled by this chain
                 * requires ADMIN role.
                 */
                .anyRequest()
                .hasRole("ADMIN")
            )


            // =================================================
            // ADMIN FORM LOGIN
            // =================================================

            .formLogin(form -> form

                /*
                 * Admin login page.
                 */
                .loginPage("/adminlogin")

                /*
                 * Admin login form POSTs here.
                 */
                .loginProcessingUrl("/adminlogin")

                /*
                 * After successful admin authentication,
                 * our custom handler will run.
                 */
                .successHandler(successHandler)

                /*
                 * If admin login fails.
                 */
                .failureUrl("/adminlogin?error=true")

                .permitAll()
            )


            // =================================================
            // LOGOUT
            // =================================================

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/userlogin?logout=true")

                .invalidateHttpSession(true)

                .clearAuthentication(true)

                .deleteCookies("JSESSIONID")

                .permitAll()
            );


        return http.build();
    }


    // =========================================================
    // MAIN / USER SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(
            HttpSecurity http,
            CustomAuthenticationSuccessHandler successHandler)
            throws Exception {

        http


            // =================================================
            // AUTHORIZATION
            // =================================================

            .authorizeHttpRequests(auth -> auth

                // -------------------------------------------------
                // Static resources
                // -------------------------------------------------

                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**"
                )
                .permitAll()


                // -------------------------------------------------
                // Authentication pages
                // -------------------------------------------------

                .requestMatchers(
                    "/userlogin",
                    "/signup"
                )
                .permitAll()


                // -------------------------------------------------
                // Public store pages
                // -------------------------------------------------

                .requestMatchers(
                    "/",
                    "/products",
                    "/products/**",
                    "/search",
                    "/category/**",
                    "/privacy-policy",
                    "/terms-and-conditions"
                )
                .permitAll()


                // -------------------------------------------------
                // USER AREA
                // -------------------------------------------------

                .requestMatchers(
                	    "/dashboard",
                	    "/account-settings",
                	    "/account-settings/**",
                	    "/cart",
                	    "/cart/**",
                	    "/checkout",
                	    "/checkout/**",
                	    "/my-orders",
                	    "/my-orders/**",
                	    "/saved-items",
                	    "/saved-items/**",
                	    "/wishlist",
                	    "/wishlist/**"
                	)
                	.hasAnyRole("USER", "ADMIN")


                // -------------------------------------------------
                // Everything else
                // -------------------------------------------------

                .anyRequest()
                .permitAll()
            )


            // =================================================
            // USER FORM LOGIN
            // =================================================

            .formLogin(form -> form

                /*
                 * User login page.
                 */
                .loginPage("/userlogin")

                /*
                 * User login form POSTs here.
                 */
                .loginProcessingUrl("/userlogin")

                /*
                 * Successful authentication.
                 */
                .successHandler(successHandler)

                /*
                 * Failed authentication.
                 */
                .failureUrl("/userlogin?error=true")

                .permitAll()
            )


            // =================================================
            // LOGOUT
            // =================================================

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/userlogin?logout=true")

                .invalidateHttpSession(true)

                .clearAuthentication(true)

                .deleteCookies("JSESSIONID")

                .permitAll()
            );


        return http.build();
    }
}