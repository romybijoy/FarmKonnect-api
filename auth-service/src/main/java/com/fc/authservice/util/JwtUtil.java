package com.fc.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating and validating JWT tokens.
 * This class loads the JWT secret from application properties,
 * decodes it from Base64, and uses it to sign and verify tokens.
 */
@Slf4j
@Getter
@Component
public class JwtUtil {

    private final Key secretKey;

    /**
     * Loads and decodes the JWT secret key from application properties.
     *
     * @param secret Base64 encoded secret value defined in application.yml
     */
    @Autowired
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        log.debug("Injected Secret: {}", secret);

        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret is missing or not injected!");
        }

        // Decode Base64 secret into bytes
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));

        // Create an HMAC SHA key for signing JWTs
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        log.info("JWT secret key successfully initialized.");
    }

    /**
     * Generates a JWT token with email and role claims.
     *
     * @param userId the subject of the JWT
     * @param role  user role
     * @return signed JWT token string
     */
    public String generateToken(UUID userId, String email, String role) {
        log.debug("Generating JWT token for userId={}, role={}", userId, role);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *10)) // 10 hours
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a JWT token and throws an exception if invalid.
     *
     * @param token JWT token string
     */
    public void validateToken(String token) {
        try {
            log.debug("Validating JWT token...");

            Jwts.parser()
                    .verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);

            log.debug("JWT token is valid.");
        } catch (SignatureException e) {
            log.error("Invalid JWT signature", e);
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e) {
            log.error("Invalid JWT token", e);
            throw new JwtException("Invalid JWT");
        }
    }

    /**
     * Validates a JWT token and returns its claims.
     *
     * @param token JWT token string
     * @return Claims extracted from the token
     */
    public Claims validateTokenAndGetClaims(String token) {
        log.debug("Validating token and extracting claims...");

        return Jwts
                .parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
