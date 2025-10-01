package com.fc.postservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    private UUID userId;
    private String content;
    private UUID parentId; // optional, for replies
}
