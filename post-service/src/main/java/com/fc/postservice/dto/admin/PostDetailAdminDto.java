package com.fc.postservice.dto.admin;

import com.fc.postservice.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Detailed DTO used for Admin viewing of a specific post.
 * This DTO includes:
 *  - Base post content
 *  - User profile details
 *  - Engagement metrics (likes, comments, saves)
 *  - Repost metadata when applicable
 * Displayed on:
 *  - Admin Post Detail Page
 *  - Reporting dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailAdminDto {

    /** Unique ID of the post */
    private UUID postId;

    /** ID of the user who created the post */
    private UUID userId;

    /** Display name of the post author */
    private String userName;

    /** Full text content of the post */
    private String content;

    /** List of image URLs attached to the post */
    private List<String> postImages;

    /** Author's district (for admin metadata display) */
    private String district;

    /** Author's profile description (bio) */
    private String description;

    /** Timestamp when the post was originally created */
    private LocalDateTime createdAt;

    // --- Engagement Metrics ---

    /** Total number of likes on the post */
    private long likeCount;

    /** Total number of comments on the post */
    private long commentCount;

    /** Count of users who saved this post */
    private long saveCount;

    // --- Repost Metadata (Optional) ---

    /** Indicates if the post is a repost */
    private boolean isRepost;

    /** ID of the original post (if this is a repost) */
    private UUID originalPostId;

    private PostStatus status;

    /** ID of the user who reposted this post */
    private UUID repostedBy;

    /** Timestamp of when the repost occurred */
    private LocalDateTime repostedAt;
}

