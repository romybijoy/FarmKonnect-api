package com.fc.chatservice.controller;

import com.fc.chatservice.dto.TypingStatusDTO;
import com.fc.chatservice.model.ChatMessage;
import com.fc.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("")
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
            // Send to receiver
            messagingTemplate.convertAndSend("/topic/private/" + saved.getReceiverId(), saved);

            // Also send to sender (so that sender sees their own message as realtime confirmation)
            messagingTemplate.convertAndSend("/topic/private/" + saved.getSenderId(), saved);
        }
    }

    @MessageMapping("/typing")
    public void handleTyping(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Boolean isTyping = (Boolean) payload.get("isTyping");

        if (email == null || isTyping == null) return;
        System.out.println("Typing received: email=" + email + ", isTyping=" + isTyping);
        if (payload.containsKey("receiverId")) {
            // Private chat typing
            String receiverId = (String) payload.get("receiverId");

            messagingTemplate.convertAndSendToUser(
                    receiverId,
                    "/queue/typing",
                    Map.of("email", email, "isTyping", isTyping)
            );

        } else if (payload.containsKey("groupId")) {
            // Group chat typing
            String groupId = (String) payload.get("groupId");

            messagingTemplate.convertAndSend(
                    "/topic/typing/group/" + groupId,
                    Map.of("email", email, "isTyping", isTyping)
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
            messagingTemplate.convertAndSend("/topic/private/" + saved.getReceiverId(), saved);
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
}
