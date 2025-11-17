package com.fc.feedservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PostDto {
    private UUID id;
    private UUID userId;
    private String content;
    private List<String> postImages;
    private String userName;
    private String description;
    private String image;
    private String district;
    private LocalDateTime createdAt;


    private boolean likedByCurrentUser;
    private int likeCount;
    private boolean savedByCurrentUser;


    private boolean isRepost;
    private UUID repostedBy;
    private UUID originalPostId;
    private String repostedByName;
    private String repostedByImage;
    private LocalDateTime repostedAt;

    // ✅ Nested Original Post
    private PostDto originalPost;
}
