package com.fc.postservice.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for representing post information
 * in the Admin Dashboard.
 * This DTO is used for:
 *  - Post moderation list view
 *  - Admin analytics pages
 *  - Reporting and moderation workflows
 * Includes:
 *  - Basic post details
 *  - Engagement metrics (likes, comments, saves)
 *  - Repost metadata
 */
@Getter
@Setter
@Builder
public class AdminPostDto {
    /** Unique ID of the post */
    private UUID postId;

    /** ID of the user who created the post */
    private UUID userId;

    /** Display name of the post owner */
    private String userName;

    /** Shortened preview of the post text */
    private String contentPreview;

    /** List of image URLs attached to the post */
    private List<String> postImages;

    /** Timestamp when the post was created */
    private LocalDateTime createdAt;

    /** Indicates if this post is a repost */
    private boolean isRepost;

    /** Original post ID (if repost) */
    private UUID originalPostId;

    /** Number of comments on the post */
    private int commentCount;

    /** Number of times this post was saved */
    private int saveCount;

    /** Number of likes this post received */
    private int likeCount;

}
