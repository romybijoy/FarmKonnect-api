package com.fc.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for setting up WebSocket and STOMP messaging.
 * This enables real-time notification delivery using:
 * - A STOMP endpoint for client connections
 * - A message broker for broadcasting messages
 * - User-specific queues for private notifications
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the STOMP message broker responsible for routing messages.
     * Key elements:
     *  - enableSimpleBroker: Enables an in-memory message broker for destinations like /queue
     *  - setApplicationDestinationPrefixes: Prefix clients must use when sending messages to @MessageMapping
     *  - setUserDestinationPrefix: Allows sending messages to specific users (/user/{id}/queue/notifications)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Messages sent to /queue/... will be handled by the simple broker
        config.enableSimpleBroker("/queue");

        // Client-side sends messages to /app/... → handled by @MessageMapping in controllers
        config.setApplicationDestinationPrefixes("/app");

        // Enables sending notifications to specific users using /user/{sessionId}/queue/...
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registers WebSocket STOMP endpoints that clients will connect to.
     * SockJS is enabled to provide fallback options for browsers that
     * do not support WebSocket natively.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("*")// Allow all origins (configure properly in production)
                .withSockJS();                 // Enable SockJS fallback
    }
}