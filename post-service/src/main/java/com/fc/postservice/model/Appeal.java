package com.fc.postservice.model;

import com.fc.postservice.enums.AppealStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Appeal for reported post
 *
 * @author Romyb
 * @since 23/02/2026
 */
@Entity
@Table(name = "appeals")
@Data
public class Appeal {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID postId;

    private UUID userId; // owner

    private String reason;

    @Enumerated(EnumType.STRING)
    private AppealStatus status;

    private Instant createdAt;
    private Instant reviewedAt;

    private UUID reviewedBy;
}
