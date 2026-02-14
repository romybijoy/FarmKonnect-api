package com.fc.chatservice.dto;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReactionEvent {
    private UUID messageId;
    private UUID userId;
    private String emoji;
    private String type; // "ADD", "REMOVE", "UPDATE"

    private UUID groupId;
    private UUID senderId;
    private UUID receiverId;

    private String eventType;
}