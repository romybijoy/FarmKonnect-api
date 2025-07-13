package com.fc.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name= "chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID senderId;
    private UUID receiverId; // null for group

    private UUID groupId;  // Nullable if 1-1 chat
    private String content;
    private String type; // e.g., "TEXT", "IMAGE"
    private String fileUrl;
    private LocalDateTime timestamp;


}
