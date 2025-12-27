package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;


/**
 * Entity representing a permanent audit log of actions taken by administrators
 * on posts or reports.
 * Each audit record documents:
 *  - Which admin performed the action (actionBy)
 *  - What action was taken (actionType)
 *  - Why the action was taken (actionReason, optional)
 *  - The affected post or report
 *  - Timestamp when the action occurred
 * This log is crucial for transparency, compliance, and admin history tracking.
 */
@Entity
@Table(name = "moderation_audit")
@Data
public class ModerationAudit {

    /**
     * Unique identifier for the audit record.
     * Stored as BINARY(16) for efficient storage and indexing of UUIDs.
     */
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * ID of the report associated with this moderation action.
     * Nullable since some moderation actions may not originate from a user report.
     */
    @Column(name = "report_id", columnDefinition = "BINARY(16)")
    private UUID reportId;

    /**
     * ID of the post affected by this moderation action.
     * Nullable for actions that concern only the report.
     */
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private UUID postId;

    /**
     * ID of the administrator who performed the action.
     */
    @Column(name = "action_by", columnDefinition = "BINARY(16)")
    private UUID actionBy;

    /**
     * Type of moderation action performed.
     * Examples:
     *  - REMOVE_POST
     *  - RESTORE_POST
     *  - APPROVE_REPORT
     *  - DISMISS_REPORT
     */
    @Column(name = "action_type")
    private String actionType;

    /**
     * Optional explanation for why the moderator took this action.
     * Stored as TEXT to allow long-form reasoning.
     */
    @Column(name = "action_reason", columnDefinition = "TEXT")
    private String actionReason;

    /**
     * Timestamp when the action was logged.
     * Automatically set at creation and never updated.
     */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /**
     * Ensures UUID and timestamp are automatically initialized
     * before the entity is persisted.
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

