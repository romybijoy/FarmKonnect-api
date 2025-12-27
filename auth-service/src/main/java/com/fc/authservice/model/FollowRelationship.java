package com.fc.authservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a follow relationship between two users.
 * This table stores information about which user (followerId) is
 * following another user (followingId). Each pair is unique due to
 * the @UniqueConstraint on (follower_id, following_id).
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "follow_relationships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
public class FollowRelationship {

    /** Primary key for follow relationship entry */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** ID of the user who follows another user */
    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    /** ID of the user who is being followed */
    @Column(name = "following_id", nullable = false)
    private UUID followingId;

    /** Timestamp of when the follow action occurred */
    @Column(name = "followed_at")
    private LocalDateTime followedAt = LocalDateTime.now();

    /**
     * Default constructor required by JPA.
     */
    public FollowRelationship() {
        // JPA default constructor
    }
}
