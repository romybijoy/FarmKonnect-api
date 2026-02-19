package com.fc.feedservice.dto;

import com.postservice.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing a post displayed in the user's feed.
 * Includes post content, author details, repost metadata,
 * engagement details (likes & saves), and nested original post info
 * when the post is a repost.
 */
@Data
@Builder
@Schema(description = "Represents a post item in the user's feed")
public class PostDto {
    // ----------------------------- Basic Post Info -----------------------------

    @Schema(description = "ID of the post")
    private UUID id;

    @Schema(description = "ID of the original creator of the post")
    private UUID userId;

    @Schema(description = "Text content of the post")
    private String content;

    @Schema(description = "List of image URLs attached to the post")
    private List<String> postImages;

    @Schema(description = "Username of the post creator")
    private String userName;

    @Schema(description = "Short bio or description of the user")
    private String description;

    @Schema(description = "Profile image URL of the user")
    private String image;

    @Schema(description = "User's district, used for feed suggestions")
    private String district;

    @Schema(description = "Timestamp when the post was created")
    private LocalDateTime createdAt;


    // ----------------------------- Engagement Info -----------------------------

    @Schema(description = "Whether the current viewer has liked this post")
    private boolean likedByCurrentUser;

    @Schema(description = "Total number of likes on this post")
    private int likeCount;

    @Schema(description = "Whether the current viewer has saved this post")
    private boolean savedByCurrentUser;


    // ----------------------------- Repost Metadata -----------------------------

    @Schema(description = "Indicates if this post is a repost of another")
    private boolean isRepost;

    @Schema(description = "ID of the user who reposted this post")
    private UUID repostedBy;

    @Schema(description = "ID of the original post (if this is a repost)")
    private UUID originalPostId;

    @Schema(description = "Username of the user who reposted")
    private String repostedByName;

    @Schema(description = "Profile image of the user who reposted")
    private String repostedByImage;

    @Schema(description = "Timestamp when the repost occurred")
    private LocalDateTime repostedAt;

    @Schema(description = "post Status (e.g. PENDING, APPROVED, REMOVED) - used for moderation purposes")
    private PostStatus status;
    // ----------------------------- Nested Original Post -----------------------------

    @Schema(description = "Full original post details when this post is a repost")
    private PostDto originalPost;
}
