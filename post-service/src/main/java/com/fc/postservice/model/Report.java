package com.fc.postservice.model;

import com.fc.postservice.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user report submitted against a post.
 * Includes metadata such as reporter details, reason, review status,
 * admin moderation details, and timestamps.
 */
@Entity
@Table(name = "reports")
@Data
@Schema(description = "User-submitted report for inappropriate or abusive posts.")
public class Report {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "binary(16)")
    @Schema(description = "Unique identifier for the report")
    private UUID id;

    @Column(name = "post_id", nullable = false)
    @Schema(description = "ID of the post being reported")
    private UUID postId;

    @Column(name = "reporter_id", nullable = false)
    @Schema(description = "User ID of the person who submitted the report")
    private UUID reporterId;

    @Schema(description = "Short reason selected by the reporter (e.g., spam, abuse)")
    private String reason;

    @Column(columnDefinition = "text")
    @Schema(description = "Detailed explanation of the report")
    private String details;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Current review status of the report")
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_id")
    @Schema(description = "Admin ID who reviewed the report (nullable until reviewed)")
    private UUID adminId;

    @Column(name = "created_at")
    @Schema(description = "Timestamp when the report was created")
    private Instant createdAt = Instant.now();

    @Column(name = "reviewed_at")
    @Schema(description = "Timestamp when the report was reviewed by an admin")
    private Instant reviewedAt;
}

