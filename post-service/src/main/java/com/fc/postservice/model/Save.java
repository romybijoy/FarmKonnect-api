package com.fc.postservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a "post save" action by a user.
 * Stores which user saved which post and when.
 */
@Entity
@Table(name = "saves")
@Data
@Schema(description = "Represents a saved post entry for a specific user.")
public class Save {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier for the save entry")
    private UUID id;

    @Schema(description = "ID of the user who saved the post", required = true)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @Schema(description = "Post entity that was saved by the user")
    private Post post;

    @Schema(description = "Timestamp when the post was saved")
    private LocalDateTime savedAt = LocalDateTime.now();
}

