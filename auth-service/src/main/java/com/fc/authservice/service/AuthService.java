package com.fc.authservice.service;

import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.model.User;
import com.fc.authservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Service responsible for authentication logic, JWT handling,
 * and extracting user-related claims from tokens.
 * Responsibilities:
 * - Authenticate user credentials
 * - Generate JWT tokens
 * - Validate JWT tokens
 * - Extract information (userId/subject) from tokens
 * This service interacts with UserService to fetch users,
 * and JwtUtil to handle signing and validating tokens.
 * Logging is added for:
 * - Successful authentication
 * - Failed authentication
 * - Token validation failures
 * Author: Romy Rose Jimmy
 * Since: 2025
 */
@Slf4j
@Service
public class AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

     @Lazy
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user based on email and password.
     * Returns a JWT token if authentication succeeds.
     *
     * @param loginRequestDTO incoming login request containing email & password
     * @return Optional containing the JWT token or empty if authentication fails
     */
    public Optional<String> authenticate(UsersDTO loginRequestDTO) {
        log.info("Authentication attempt for email={}", loginRequestDTO.getEmail());
        return userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordMatches(loginRequestDTO.getPassword(), u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getEmail(), u.getRole().name());
                    log.info("Authentication successful for email={}", u.getEmail());
                    return token;
                });
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        boolean match = passwordEncoder.matches(rawPassword, encodedPassword);
        if (!match) {
            log.warn("Password mismatch for login attempt");
        }
        return match;
    }

    /**
     * Validates a JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e){
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user ID (subject) from a valid JWT token.
     *
     * @param token JWT token
     * @return extracted user identifier (email or userId depending on system design)
     */
    public String extractUserId(String token) {
        // Logic to parse the token and extract user ID
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) jwtUtil.getSecretKey()) // Use SecretKey here
                .build()
                .parseSignedClaims(token)
                .getPayload();

        log.debug("Extracted subject from token: {}", claims.getSubject());
        return claims.getSubject(); // Or claims.get("userId")
    }

    /**
     * Generates a token for a given user.
     * Useful for:
     * - Social login
     * - Password reset completion
     * - OTP verification workflow
     *
     * @param user user object
     * @return Optional containing generated JWT token
     */
    public Optional<String> generateTokenForUser(User user) {
        // You may use Spring Security UserDetails, or directly user
        // Here is a simple example:

        String username = user.getEmail(); // or user.getId().toString()
        String token = jwtUtil.generateToken(username, user.getRole().name());

        log.info("Generated token for userId={} email={}", user.getId(), user.getEmail());
        return Optional.of(token);
    }
}
