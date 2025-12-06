package com.fc.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Auth Service.
 * This class defines HTTP security rules including request authorization,
 * CSRF settings, and CORS handling. Currently, all requests are allowed as
 * authentication is handled internally through API-level validation rather
 * than Spring Security filters.
 * Additionally, this class provides the PasswordEncoder bean used for
 * securely hashing user passwords using BCrypt.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the HTTP security filter chain.
     * - Permits all incoming requests
     * - Disables CSRF protection
     * - Enables CORS using default configuration
     * This configuration is commonly used in authentication services
     * where endpoints are protected manually via custom logic rather
     * than Spring Security's built-in authentication flow.
     *
     * @param http the HttpSecurity instance used to configure web security
     * @return a fully built SecurityFilterChain
     * @throws Exception in case of configuration errors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()); // Uses the corsConfigurationSource bean

        return http.build();
    }

    /**
     * Provides a password encoder bean for hashing user passwords.
     * BCrypt is used as it is a secure, industry-standard hashing algorithm.
     *
     * @return a PasswordEncoder implementation using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
