package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a "like" action on a post by a specific user.
 * Characteristics:
 *  - A user can like many posts
 *  - A post can have many likes
 *  - Likes are stored as individual records for analytics,
 *    counting, and avoiding duplicate likes
 * The combination of (userId, post) is typically checked in service logic
 * to prevent duplicate likes.
 */
@Entity
@Table(name = "likes")
@Data
public class Like {

    /** Unique identifier for the like entry */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** ID of the user who liked the post */
    private UUID userId;

    /**
     * The post that was liked.
     * Lazy loading avoids fetching post details unless explicitly needed.
     * A Many-to-One mapping because many likes can belong to a single post.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /** Timestamp of when the like was created */
    private LocalDateTime likedAt = LocalDateTime.now();

}

