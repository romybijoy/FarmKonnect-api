package com.fc.stories_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

/**
 * Data Transfer Object representing an incoming/outgoing Story payload.
 * Used for creating new stories and returning story details to clients.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoryDto {
    /**
     * Unique identifier of the story (may be null when creating a new story).
     */
    private UUID id;

    /**
     * Type of story content.
     * Examples: "image", "video".
     */
    private String type;

    /**
     * ID of the user who created the story.
     */
    private UUID userId;

    /**
     * Username of the story owner (used for displaying in UI).
     */
    private String username;

    /**
     * Profile picture URL of the story owner.
     */
    private String profilePic;

    /**
     * URL of the image file if this story is an image-based story.
     */
    private String imageUrl;

    /**
     * URL of the video file if this story is a video-based story.
     */
    private String videoUrl;

    /**
     * Timestamp when the story was created (string formatted for frontend).
     */
    private String timestamp;
}
