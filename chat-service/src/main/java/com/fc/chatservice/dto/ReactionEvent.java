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
}