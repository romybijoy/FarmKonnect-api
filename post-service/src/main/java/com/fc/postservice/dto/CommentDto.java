package com.fc.postservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a comment or reply in a post.
 * This structure supports nested replies through a recursive `replies` list.
 * Each CommentDto contains:
 *  - Basic comment content
 *  - User information (fetched from UserService via gRPC)
 *  - Parent-child relationship metadata
 * Used in:
 *  - Comment retrieval API
 *  - Feed display
 *  - Admin analytics (optional)
 */
@Data
@Builder
public class CommentDto {
    /** Unique identifier of the comment */
    private UUID id;

    /** Actual text content of the comment */
    private String content;

    /** ID of the post to which this comment belongs */
    private UUID postId;

    /** ID of the parent comment if this is a reply, otherwise null */
    private UUID parentId;

    /** Timestamp of when the comment was created */
    private LocalDateTime createdAt;

    // -------- User Information (from gRPC UserService) --------

    /** ID of the user who posted the comment */
    private UUID userId;

    /** Display name of the commenting user */
    private String userName;

    /** Profile image URL of the user */
    private String profileImage;

    // -------- Nested Replies --------

    /**
     * List of replies for this comment.
     * Supports unlimited depth recursion for threaded conversations.
     */
    @Builder.Default
    private List<CommentDto> replies = new ArrayList<>();

    private boolean edited;
    private boolean deleted;

    private int likeCount;
    private boolean likedByCurrentUser;
    private boolean ownedByCurrentUser;

    private long replyCount;

    private List<ReactionDto> reactions;
}
