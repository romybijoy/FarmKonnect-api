package com.fc.postservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket and STOMP configuration for the Post Service.
 * This setup enables:
 *  - Real-time messaging via STOMP
 *  - An in-memory broker for topic-based messaging
 *  - A WebSocket endpoint for client subscriptions
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the STOMP message broker.
     * enableSimpleBroker("/topic"):
     *    - Activates in-memory broker to handle messages sent to /topic/...
     * setApplicationDestinationPrefixes("/app"):
     *    - Client messages sent to /app/... will be routed to @MessageMapping methods
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // broadcast channels (e.g., /topic/posts)
        config.setApplicationDestinationPrefixes("/app"); // for messages sent from client to server
    }

    /**
     * Registers WebSocket STOMP endpoints.
     * /ws-endpoint:
     *    - Clients connect here to start a WebSocket session.
     *    - SockJS fallback is enabled for browsers that do not support WebSockets.
     * setAllowedOrigins("*"):
     *    - Allows all origins (not recommended in production—use specific domains)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-endpoint")
                .setAllowedOrigins("*") // In production, specify allowed domains
                .withSockJS(); // Enable fallback support
    }
}
