package com.fc.postservice.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Request payload used when creating a new comment or replying to an existing one.
 * Fields:
 *  - userId:      ID of the user posting the comment
 *  - content:     Text content of the comment
 *  - parentId:    Optional — if provided, the comment is treated as a reply
 * Used in:
 *  - POST /{postId}/comments
 *  - POST /{postId}/comments/{parentId}/reply
 */
@Data
public class CommentRequest {
    /** ID of the user submitting the comment */
    private UUID userId;

    /** Text content of the comment */
    private String content;

    /** Optional parent comment ID — if present, this is a reply instead of a top-level comment */
    private UUID parentId;
}
