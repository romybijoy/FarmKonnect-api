package com.fc.chatservice.config.websocket;

import com.fc.chatservice.service.UserPresenceService;
import com.fc.chatservice.util.JwtUtil;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;
import java.util.List;

@Component
public class WebSocketPresenceListener {

    private final SimpUserRegistry simpUserRegistry;
    private final UserPresenceService userPresenceService;

    private final JwtUtil jwtUtil;

    public WebSocketPresenceListener(SimpUserRegistry simpUserRegistry,
                                     UserPresenceService userPresenceService, JwtUtil jwtUtil) {
        this.simpUserRegistry = simpUserRegistry;
        this.userPresenceService = userPresenceService;
        this.jwtUtil = jwtUtil;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String email = getEmailFromHeaderOrAuth(accessor);
        System.out.println("User connected: " + email);
        if (email != null) {
            userPresenceService.setUserOnline(email);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        System.out.println("Session Attributes on Disconnect: " + accessor.getSessionAttributes());

        String email = getEmailFromHeaderOrAuth(accessor);

        System.out.println("Email resolved in disconnect: " + email);

        if (email != null) {
            System.out.println("User disconnected: " + email);
            userPresenceService.setUserOffline(email); // remove from Redis
        } else {
            System.out.println("Could not extract email on disconnect.");
        }
    }

    private String getEmailFromHeaderOrAuth(StompHeaderAccessor accessor) {
        // ✅ 1. Try to get email from Principal (best source)
        Principal principal = accessor.getUser();
        if (principal != null) {
            return principal.getName(); // typically the email
        }

        // ✅ 2. Fallback to JWT token in native headers
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    return jwtUtil.extractEmail(token); // Decode JWT securely
                } catch (Exception e) {
                    System.err.println("Invalid JWT during disconnect: " + e.getMessage());
                }
            }
        }

        if (accessor.getSessionAttributes() != null) {
            Object email = accessor.getSessionAttributes().get("email");
            if (email != null) {
                return email.toString();
            }
        }
        return null;
    }



}


