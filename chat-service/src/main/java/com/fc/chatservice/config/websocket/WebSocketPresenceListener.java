package com.fc.chatservice.config.websocket;

import com.fc.chatservice.service.UserPresenceService;
import com.fc.chatservice.util.JwtUtil;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketPresenceListener {

    private final UserPresenceService userPresenceService;

    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPresenceListener.class);

    public WebSocketPresenceListener(UserPresenceService userPresenceService, JwtUtil jwtUtil) {
        this.userPresenceService = userPresenceService;
        this.jwtUtil = jwtUtil;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = resolveUserId(accessor);
        if (userId != null) {
            userPresenceService.setUserOnline(UUID.fromString(userId));
            logger.info("User connected: {}", userId);
        } else {
            logger.warn("WebSocket CONNECT without Principal");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = resolveUserId(accessor);
        if (userId != null) {
            userPresenceService.setUserOffline(UUID.fromString(userId));
            logger.info("User disconnected: {}", userId);
        } else {
            logger.warn("Disconnect without principal");
        }
    }



    private String getEmailFromHeaderOrAuth(StompHeaderAccessor accessor) {
        // 1. Try to get email from Principal (best source)
        Principal principal = accessor.getUser();
        if (principal != null) {
            return principal.getName(); // typically the email
        }

        // 2. Fallback to JWT token in native headers
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return jwtUtil.extractEmail(token); // Decode JWT securely
            } catch (Exception e) {
                logger.error("Invalid JWT during disconnect: {}", e.getMessage(), e);
            }
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object email = sessionAttributes.get("email");

            if (email != null) {
                return email.toString();
            }
        }
        return null;
    }

    private String resolveUserId(StompHeaderAccessor accessor) {

        // 1️⃣ Best case: Principal exists
        Principal principal = accessor.getUser();
        if (principal != null) {
            return principal.getName();
        }

        // 2️⃣ Fallback: session attributes (MOST IMPORTANT)
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object userId = sessionAttributes.get("userId");
            if (userId != null) {
                return userId.toString();
            }
        }

        return null;
    }


}
