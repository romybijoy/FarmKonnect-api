package com.fc.postservice.enums;

/**
 * Enumeration representing the moderation or visibility status of a post.
 * Used across:
 *  - Post entity
 *  - FeedService filtering logic
 *  - Admin moderation workflows
 *  - Report review actions
 * This enum helps determine whether a post should be visible to users.
 */
public enum PostStatus {

    /**
     * Post created and waiting for AI moderation.
     */
    PENDING,

    /**
     * Approved by AI / moderation and visible in feeds.
     */
    ACTIVE,

    /**
     * Rejected by AI moderation.
     */
    REJECTED,

    /**
     * Removed manually by admin.
     */
    REMOVED,

    /**
     * Reported by users but not yet reviewed.
     */
    REPORTED
}
