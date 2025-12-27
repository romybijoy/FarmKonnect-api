package com.fc.feedservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a post hidden by a user in their feed.
 * When a user hides a post, an entry is stored in this table so
 * the Feed Service can exclude that post from their personalized feed.
 */
@Entity
@Table(name = "hidden_posts")
@Data
public class HiddenPost {
    /** Primary key for the hidden post entry */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** ID of the user who hid the post */
    private UUID userId;

    /** ID of the post being hidden */
    private UUID postId;

    /** Timestamp indicating when the post was hidden */
    private LocalDateTime hiddenAt = LocalDateTime.now();
}
