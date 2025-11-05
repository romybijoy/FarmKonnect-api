package com.fc.postservice.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdminPostDto {
    public UUID postId;
    public UUID userId;
    public String userName;
    public String contentPreview;
    public String postImage;
    public LocalDateTime createdAt;
    public boolean isRepost;
    public UUID originalPostId;
    public int commentCount;
    public int saveCount;
    public int likeCount;

    public AdminPostDto(UUID postId, UUID userId, String userName, String contentPreview,
                        String postImage, LocalDateTime createdAt, boolean isRepost,
                        UUID originalPostId, int commentCount, int saveCount, int likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.contentPreview = contentPreview;
        this.postImage = postImage;
        this.createdAt = createdAt;
        this.isRepost = isRepost;
        this.originalPostId = originalPostId;
        this.commentCount = commentCount;
        this.saveCount = saveCount;
        this.likeCount = likeCount;
    }
}
