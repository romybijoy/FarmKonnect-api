package com.fc.chatservice.config.websocket;

import com.fc.chatservice.service.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private AuthServiceClient authServiceClient;  // You call auth microservice from here

    public StompAuthChannelInterceptor() {
        System.out.println("StompAuthChannelInterceptor loaded");
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // remove "Bearer "
                // validate token...

                boolean valid = authServiceClient.validateToken(token);
                System.out.println(valid);
                if (!valid) {
                    throw new IllegalArgumentException("Invalid token");
                }
                System.out.println("Received token: " + token);
            } else {
                System.out.println("Missing/invalid token, rejecting...");
                throw new IllegalArgumentException("Missing or invalid Authorization token");
            }

//            // You can store userId as session attribute if you want:
//            String userId = authServiceClient.getUserId(token);
//            accessor.setUser(new StompPrincipal(userId));
        }

        return message;
    }
}
