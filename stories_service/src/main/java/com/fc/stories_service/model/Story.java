package com.fc.stories_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Story posted by a user.
 * A story can be an image or a video and automatically expires after a set duration (e.g., 24 hours).
 */
@Data
@Entity
@Table(name= "stories")
public class Story {
    /**
     * Unique identifier for the story.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Display name of the user who posted the story.
     * This is denormalized for fast UI rendering.
     */
    @Column(name = "user_name", nullable = false)
    private String username;

    /**
     * ID of the user who owns the story.
     */
    private UUID userId;

    /**
     * Profile picture URL of the story owner.
     * Stored here to avoid calling UserService repeatedly.
     */
    private String profilePic;

    /**
     * Image URL for image-based stories.
     * Only populated when the story type is "image".
     */
    private String imageUrl;

    /**
     * Video URL for video-based stories.
     * Marked as TEXT to allow large URLs or encoded metadata.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    /**
     * Type of the story:
     * - "image"
     * - "video"
     */
    private String type;

    /**
     * Time when the story was created.
     */
    private LocalDateTime createdAt;

    /**
     * Time when the story expires.
     * Usually `createdAt + 24 hours`, but can be configured.
     */
    private LocalDateTime expiresAt;
}
