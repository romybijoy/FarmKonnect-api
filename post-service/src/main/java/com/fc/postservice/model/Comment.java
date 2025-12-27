package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a comment on a post.
 * Supports nested threaded replies using a self-referencing parent-child
 * relationship structure:
 *  - A top-level comment has parentComment = null
 *  - A reply has a parentComment assigned
 *  - replies list contains all nested child comments
 * Cascade and orphanRemoval ensure comment trees remain consistent.
 */
@Entity
@Table(name = "comments")
@Data
public class Comment {

    /** Unique identifier for the comment */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** ID of the post that this comment belongs to */
    @Column(nullable = false)
    private UUID postId;

    /** ID of the user who created the comment */
    @Column(nullable = false)
    private UUID userId;

    /** Text content of the comment (max 500 chars) */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * Parent comment reference (self-referencing ManyToOne).
     * If null → this is a top-level comment.
     * If not null → this is a reply to another comment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    /**
     * Replies to this comment.
     * CascadeType.ALL + orphanRemoval:
     *  - Persist deletes propagate to children
     *  - Removing a reply from the list deletes it from DB
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    /** Timestamp when the comment was created */
    private LocalDateTime createdAt = LocalDateTime.now();
}

