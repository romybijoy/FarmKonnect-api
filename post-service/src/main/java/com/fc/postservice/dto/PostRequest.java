package com.fc.postservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating a new post.
 * This DTO includes:
 *  - The ID of the user creating the post
 *  - Text content of the post
 *  - Optional list of image URLs
 * Used in:
 *  - POST /posts/create
 *  - Repost workflows (as part of content creation)
 */
@Data
public class PostRequest {
    /** ID of the user creating the post */
    private UUID userId;

    /** Text content of the post */
    private String content;

    /**
     * Optional list of image URLs attached to the post.
     * Can be empty or null when a post has no images.
     */
    private List<String> postImages;

}

