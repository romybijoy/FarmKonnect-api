package com.fc.postservice.dto;

import java.util.UUID;

/**
 * DTO returned to frontend. createdAt is string ISO representation.
 * Use MapStruct or manual mapper to convert from entity.
 */
public record ReportDto(
        UUID id,
        UUID postId,
        String reason,
        String details,
        String status,
        String reporterId,
        String createdAt
) {}