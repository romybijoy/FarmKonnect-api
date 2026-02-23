package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Comment Reaction
 *
 * @author Romyb
 * @since 20/02/2026
 */
@Entity
@Table(
        name = "comment_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_comment_user_reaction",
                columnNames = {"comment_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_reaction_comment", columnList = "comment_id"),
                @Index(name = "idx_reaction_user", columnList = "user_id"),
                @Index(name = "idx_reaction_emoji", columnList = "emoji")
        }
)
@Getter
@Setter
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String emoji;

    private LocalDateTime createdAt = LocalDateTime.now();
}
