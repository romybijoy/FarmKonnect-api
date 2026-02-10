package com.fc.chatservice.config.websocket;

import com.fc.chatservice.service.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.UUID;

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
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing Authorization header");
            }

            token = token.substring(7);

            // validate via auth service
            if (!authServiceClient.validateToken(token)) {
                throw new IllegalArgumentException("Invalid token");
            }

            UUID userId = authServiceClient.getUserId(token);

            logger.info("WebSocket authenticated userId={}", userId);
            // THIS is what actually persists the Principal
            accessor.setUser(new StompPrincipal(userId.toString()));

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put("userId", userId.toString());
            }
        }

        // MUST return the updated message
        return MessageBuilder.createMessage(
                message.getPayload(),
                accessor.getMessageHeaders()
        );
    }
}
