package com.fc.chatservice.controller;

import com.fc.chatservice.dto.SignalMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Video/Audio call signaling
    @MessageMapping("/call/signal")
    public void handleSignal(@Payload SignalMessage message) {
        String targetUserId = switch (message.getType()) {
            case "offer" -> message.getReceiverId();
            case "answer" -> message.getCallerId();
            case "ice" -> message.getTargetId();
            default -> "";
        };

        messagingTemplate.convertAndSend("/topic/call/" + targetUserId, message);
    }


}
