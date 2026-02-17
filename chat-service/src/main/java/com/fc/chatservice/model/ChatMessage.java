package com.fc.chatservice.model;

import com.fc.chatservice.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    // For group tracking
    @ElementCollection
    private Set<UUID> deliveredTo = new HashSet<>();

    @ElementCollection
    private Set<UUID> readBy = new HashSet<>();

}
