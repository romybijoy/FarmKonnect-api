package com.fc.postservice.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AdminPostDto {
    private UUID postId;
    private UUID userId;
    private String userName;
    private String contentPreview;
    private List<String> postImages;
    private LocalDateTime createdAt;
    private boolean isRepost;
    private UUID originalPostId;
    private int commentCount;
    private int saveCount;
    private int likeCount;

}
