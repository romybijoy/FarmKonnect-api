package com.fc.postservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a post exposed to the client (mobile/web/app).
 * Includes:
 *  - Basic post content: text, images, timestamp
 *  - User details for display
 *  - Repost metadata, including nested original post
 *  - Optional fields excluded using @JsonInclude
 * This DTO is used in:
 *  - Feed service responses
 *  - Post detail views
 *  - Saved posts
 *  - Reposts
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDTO {

    /** Unique ID of the post */
    private UUID id;

    /** Text content of the post */
    private String content;

    /** Deprecated single image field — kept for backward compatibility */
    private String image;

    /** List of image URLs associated with the post */
    private List<String> postImages;

    /** Username of the post owner */
    private String userName;

    /** User's district (location metadata) */
    private String district;

    /** Short biography/description of the post author */
    private String description;

    /** Timestamp of when the post was created */
    private LocalDateTime createdAt;

    /** ID of the user who authored the post */
    private UUID userId;

    // --------------------------------------------------------
    // Repost Metadata
    // --------------------------------------------------------

    /** Indicates whether this post is a repost */
    private boolean repost;

    /** ID of the user who reposted the content */
    private UUID repostedBy;

    /** Display name of the user who reposted */
    private String repostedByName;

    /** Profile image URL of the user who reposted */
    private String repostedByImage;

    /** Timestamp when the repost was created */
    private LocalDateTime repostedAt;

    /** ID of the original post (if this is a repost) */
    private UUID originalPostId;

    /**
     * Nested original post content.
     * Present only when this post is a repost.
     */
    private PostDTO originalPost;
}
