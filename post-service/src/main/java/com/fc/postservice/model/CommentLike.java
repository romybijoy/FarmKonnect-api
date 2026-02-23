package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comment Like
 *
 * @author Romyb
 * @since 20/02/2026
 */
@Entity
@Table(name = "comment_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "userId"}),
        indexes = {
                @Index(name = "idx_like_comment", columnList = "comment_id"),
                @Index(name = "idx_like_user", columnList = "user_id")
        })
@Getter
@Setter
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    private LocalDateTime createdAt = LocalDateTime.now();
}
