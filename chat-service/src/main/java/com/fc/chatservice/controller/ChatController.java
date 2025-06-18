package com.fc.chatservice.controller;

import com.fc.chatservice.model.ChatMessage;
import com.fc.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // For real-time messages via WebSocket
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        ChatMessage saved = chatService.saveMessage(message);
        if (saved.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group/" + saved.getGroupId(), saved);
        } else {
            messagingTemplate.convertAndSend("/topic/user/" + saved.getReceiverId(), saved);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendChatMessage(@RequestBody ChatMessage message) {
        ChatMessage saved = chatService.saveMessage(message);

        // Publish to WebSocket topic
        if (saved.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group/" + saved.getGroupId(), saved);
        } else {
            messagingTemplate.convertAndSend("/topic/user/" + saved.getReceiverId(), saved);
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<ChatMessage>> getUserChats(@PathVariable UUID userId) {
        return ResponseEntity.ok(chatService.getMessagesByUser(userId));
    }

    @GetMapping("/history/group/{groupId}")
    public ResponseEntity<List<ChatMessage>> getGroupChats(@PathVariable UUID groupId) {
        return ResponseEntity.ok(chatService.getMessagesByGroup(groupId));
    }
}
