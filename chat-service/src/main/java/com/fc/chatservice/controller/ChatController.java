package com.fc.chatservice.controller;

import com.fc.chatservice.dto.DeleteMessageRequest;
import com.fc.chatservice.model.ChatMessage;
import com.fc.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("")
public class ChatController {

    private static final String PRIVATE_TOPIC_PREFIX = "/topic/private/";
    private static final String EMAIL = "email";
    private static final String TYPING = "isTyping";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ChatService chatService;

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage message) {

        ChatMessage saved = chatService.saveMessage(message);

        if (saved.getGroupId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/group/" + saved.getGroupId(),
                    saved
            );
        } else {
            // SEND TO BOTH USERS ALWAYS
            messagingTemplate.convertAndSendToUser(
                    saved.getSenderId().toString(),
                    "/queue/messages",
                    saved
            );
            messagingTemplate.convertAndSendToUser(
                    saved.getReceiverId().toString(),
                    "/queue/messages",
                    saved
            );
        }
    }


    @MessageMapping("/typing")
    public void handleTyping(Map<String, Object> payload) {
        String email = (String) payload.get(EMAIL);
        Boolean isTyping = (Boolean) payload.get(TYPING);

        if (email == null || isTyping == null) return;
        logger.debug("Typing received: email={}, isTyping={}", email, isTyping);
        if (payload.containsKey("receiverId")) {
            // Private chat typing
            String receiverId = (String) payload.get("receiverId");

            messagingTemplate.convertAndSendToUser(
                    receiverId,
                    "/queue/typing",
                    Map.of(EMAIL, email, TYPING, isTyping)
            );

        } else if (payload.containsKey("groupId")) {
            // Group chat typing
            String groupId = (String) payload.get("groupId");

            messagingTemplate.convertAndSend(
                    "/topic/typing/group/" + groupId,
                    Map.of(EMAIL, email, TYPING, isTyping)
            );
        }
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendChatMessage(@RequestBody ChatMessage message) {
        ChatMessage saved = chatService.saveMessage(message);

        // Publish to WebSocket topic
        if (saved.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group/" + saved.getGroupId(), saved);
        } else {
            messagingTemplate.convertAndSend(PRIVATE_TOPIC_PREFIX + saved.getReceiverId(), saved);
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history/private")
    public ResponseEntity<List<ChatMessage>> getPrivateChat(
            @RequestParam UUID senderId,
            @RequestParam UUID receiverId) {

        return ResponseEntity.ok(chatService.getPrivateChat(senderId, receiverId));
    }

    @GetMapping("/history/group/{groupId}")
    public ResponseEntity<List<ChatMessage>> getGroupChats(@PathVariable UUID groupId) {
        return ResponseEntity.ok(chatService.getMessagesByGroup(groupId));
    }

    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(
            DeleteMessageRequest request
    ) {

        UUID userId = request.getUserId();

        ChatMessage updatedMessage;

        if ("DELETE_FOR_ME".equals(request.getDeleteType())) {

            updatedMessage = chatService.deleteForMe(
                    request.getMessageId(),
                    userId
            );

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/delete",
                    updatedMessage
            );

        } else if ("DELETE_FOR_EVERYONE".equals(request.getDeleteType())) {

            updatedMessage = chatService.deleteForEveryone(
                    request.getMessageId(),
                    userId
            );

            if (updatedMessage.getGroupId() != null) {

                messagingTemplate.convertAndSend(
                        "/topic/group/" + updatedMessage.getGroupId(),
                        updatedMessage
                );

            } else {

                messagingTemplate.convertAndSendToUser(
                        updatedMessage.getSenderId().toString(),
                        "/queue/messages",
                        updatedMessage
                );

                messagingTemplate.convertAndSendToUser(
                        updatedMessage.getReceiverId().toString(),
                        "/queue/messages",
                        updatedMessage
                );
            }
        }
    }


}
