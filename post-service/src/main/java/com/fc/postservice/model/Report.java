package com.fc.postservice.model;

import com.fc.postservice.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
public class Report {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "binary(16)")
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    private String reason;

    @Column(columnDefinition = "text")
    private String details;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_id")
    private UUID adminId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

}

