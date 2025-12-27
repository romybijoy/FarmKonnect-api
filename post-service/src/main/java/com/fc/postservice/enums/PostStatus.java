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
     * The post is visible to users and appears in feeds.
     */
    ACTIVE,

    /**
     * The post has been removed by an admin or moderation action.
     * Removed posts are excluded from user feeds and interactions.
     */
    REMOVED
}
