package com.fc.postservice.model;

import com.fc.postservice.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

/**
 * Entity representing a user post in the platform.
 * Includes metadata such as images, repost info, and moderation status.
 */
@Entity
@Table(name= "post")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Post entity representing user-generated content with media, reposting and moderation details.")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier of the post")
    private UUID id;

    @Column(length = 1000)
    @Schema(description = "Main text content of the post", maxLength = 1000)
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url", length = 1200)
    @Schema(description = "List of image URLs attached to the post")
    private List<String> postImages = new ArrayList<>();

    @Schema(description = "Profile image of the user who created the post")
    private String image;

    @Schema(description = "Name of the user who created the post")
    private String userName;

    @Schema(description = "District of the user")
    private String district;

    @Schema(description = "Short description or caption of the post")
    private String description;

    @Schema(description = "Timestamp when the post was created")
    private LocalDateTime createdAt;

    @Schema(description = "User ID who created the post")
    private UUID userId;

    @Schema(description = "List of comment IDs belonging to this post")
    private List<Long> commentIds;

    // -------------------------------------------------------------------------
    // Repost Details
    // -------------------------------------------------------------------------

    @Schema(description = "Indicates whether the post is a repost")
    private boolean isRepost = false;

    @Column(name = "original_post_id")
    @Schema(description = "The ID of the original post that was reposted")
    private UUID originalPostId;

    @Schema(description = "User ID of the person who reposted")
    private UUID repostedBy;

    @Schema(description = "Timestamp when the repost occurred")
    private LocalDateTime repostedAt;

    // -------------------------------------------------------------------------
    // Moderation Details
    // -------------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Schema(description = "Current moderation status of the post")
    @Column(nullable = false)
    private PostStatus status = PostStatus.PENDING;

    @Column(columnDefinition = "BINARY(16)")
    @Schema(description = "Admin user ID who removed the post")
    private UUID removedBy;

    @Schema(description = "Timestamp when the post was removed")
    private Instant removedAt;

    @Column(name = "moderation_reason", columnDefinition = "TEXT")
    @Schema(description = "Reason for removal or moderation action")
    private String moderationReason;

    private Double aiScore;

}
