package com.fc.chatservice.controller;

import com.fc.chatservice.dto.SignalMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/signal")
    public void handleSignal(SignalMessage signal, Principal principal) {
        String toUserId = signal.getTo();
        messagingTemplate.convertAndSendToUser(toUserId, "/topic/signal", signal);
    }
}
