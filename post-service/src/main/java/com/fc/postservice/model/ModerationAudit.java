package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "moderation_audit")
@Data
public class ModerationAudit {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "report_id", columnDefinition = "BINARY(16)")
    private UUID reportId;

    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private UUID postId;

    @Column(name = "action_by", columnDefinition = "BINARY(16)")
    private UUID actionBy;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "action_reason", columnDefinition = "TEXT")
    private String actionReason;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

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

