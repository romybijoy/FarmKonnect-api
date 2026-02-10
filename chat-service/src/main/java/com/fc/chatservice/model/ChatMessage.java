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

    @Column(columnDefinition = "TEXT")
    private String content;
    private String type; // e.g., "TEXT", "IMAGE"

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    private String fileName;
    private LocalDateTime timestamp;

    private Boolean deletedForAll = false;

    @Column(columnDefinition = "TEXT")
    private String deletedBy;


}
