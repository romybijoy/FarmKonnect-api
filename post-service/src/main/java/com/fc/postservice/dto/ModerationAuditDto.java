package com.fc.postservice.dto;

import java.util.UUID;

public record ModerationAuditDto(
        UUID id,
        String reportId,
        String postId,
        String actionBy,
        String actionType,
        String actionReason,
        String createdAt
) {}
