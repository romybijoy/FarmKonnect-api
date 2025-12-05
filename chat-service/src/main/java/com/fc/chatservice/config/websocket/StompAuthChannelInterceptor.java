package com.fc.chatservice.config.websocket;

import com.fc.chatservice.service.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private AuthServiceClient authServiceClient;  // You call auth microservice from here

    private static final Logger logger = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

    @PostConstruct
    public void init() {
        logger.info("StompAuthChannelInterceptor loaded");
    }

    @Override
    public @Nullable Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // Skip non-STOMP messages (SockJS INFO, handshake, heartbeats)
        if (accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // remove "Bearer "
                // validate token...

                boolean valid = authServiceClient.validateToken(token);
                logger.debug("Token valid: {}", valid);
                if (!valid) {
                    throw new IllegalArgumentException("Invalid token");
                }
                logger.debug("Received token: {}", token);

                // You can store userId as session attribute if you want:
                String userId = authServiceClient.getUserId(token);
                logger.debug("Authenticated WebSocket userId: {}", userId);

                accessor.setUser(new StompPrincipal(userId));
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("email", userId);
                } else {
                    logger.debug("Session attributes are not available to store user email");
                }
            } else {
                logger.warn("Missing/invalid token, rejecting...");
                throw new IllegalArgumentException("Missing or invalid Authorization token");
            }

        }

        return message;
    }
}
