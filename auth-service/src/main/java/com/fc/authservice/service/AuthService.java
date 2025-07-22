package com.fc.authservice.service;

import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

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

    public Optional<String> authenticate(UsersDTO loginRequestDTO) {
        Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(),
                        u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole().name()));

        return token;
    }


    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }

    public String extractUserId(String token) {
        // Logic to parse the token and extract user ID
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) jwtUtil.getSecretKey()) // Use SecretKey here
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject(); // Or claims.get("userId")
    }
}
