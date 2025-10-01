package com.fc.postservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommentDto {
    private UUID id;
    private String content;
    private UUID postId;
    private UUID parentId;
    private LocalDateTime createdAt;

    // user info (from gRPC call)
    private UUID userId;
    private String userName;
    private String profileImage;

    @Builder.Default
    private List<CommentDto> replies = new ArrayList<>();
}
