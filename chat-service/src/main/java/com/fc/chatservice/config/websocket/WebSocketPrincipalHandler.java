package com.fc.chatservice.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * User Handshake Handler
 *
 * @author Romyb
 * @since 02/01/2026
 */
@Component
public class WebSocketPrincipalHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object userId = attributes.get("userId");

        if (userId == null) {
            throw new IllegalStateException("❌ userId NOT found in WS attributes");
        }

        return new StompPrincipal(userId.toString());
    }
}


