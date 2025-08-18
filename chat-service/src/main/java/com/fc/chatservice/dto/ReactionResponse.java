package com.fc.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ReactionResponse {
    private String emoji;
    private UUID userId;
    private String userName;
    private String userImage;
}
