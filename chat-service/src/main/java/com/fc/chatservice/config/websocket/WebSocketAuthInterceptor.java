package com.fc.chatservice.config.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import io.jsonwebtoken.Jwts;


import javax.crypto.SecretKey;
import java.util.Map;

/**
 * Handshake interceptor
 *
 * @author Romyb
 * @since 02/01/2026
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (request instanceof ServletServerHttpRequest servletRequest) {

            String authHeader =
                    servletRequest.getServletRequest()
                            .getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // MUST be userId (same as frontend & DB)
                String userId = claims.getSubject();

                attributes.put("userId", userId);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {}
}

