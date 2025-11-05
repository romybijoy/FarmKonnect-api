package com.fc.postservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailAdminDto {

    private UUID id;                // Post ID
    private UUID userId;            // Author ID
    private String userName;        // Author name
    private String content;         // Text content
    private String postImage;       // Attached image
    private String district;        // User district
    private String description;     // Additional description
    private LocalDateTime createdAt;// When post was created

    // --- Counts ---
    private long likeCount;
    private long commentCount;
    private long saveCount;

    // --- Repost info (optional) ---
    private boolean isRepost;
    private UUID originalPostId;
    private UUID repostedBy;
    private LocalDateTime repostedAt;
}

