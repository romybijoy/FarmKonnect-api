package com.fc.chatservice.controller;

import com.fc.chatservice.dto.SignalMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);

    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Video/Audio call signaling
    @MessageMapping("/call/signal")
    public void handleSignal(@Payload SignalMessage message) {

        logger.debug("[Signal Received] {}", message);

        // ✅ Log just the callType
        logger.info("[Signal Received] callType: {}", message.getCallType());

        String targetUserId = switch (message.getType()) {
            case "offer", "answer", "end" -> message.getReceiverId();
            case "ice" -> message.getTargetId();
            default -> "";
        };

        messagingTemplate.convertAndSend("/topic/call/" + targetUserId, message);
    }


}
