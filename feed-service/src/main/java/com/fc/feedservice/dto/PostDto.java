package com.fc.feedservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PostDto {
    private UUID id;
    private UUID userId;
    private String content;
    private String postImage;
    private String userName;
    private String description;
    private String image;
    private String district;
    private LocalDateTime createdAt;
    private boolean isRepost = false;
    private UUID originalPostId;
    private UUID repostedBy;
    private LocalDateTime repostedAt;

    private boolean likedByCurrentUser;
    private int likeCount;
    private boolean savedByCurrentUser;
}
