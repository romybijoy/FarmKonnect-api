package com.fc.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO used when a user files a report.
 * Records work well here because they're immutable and Jackson supports them.
 */
public record CreateReportDto(
        @NotBlank(message = "Reason is required")
        @Size(max = 100, message = "Reason too long")
        String reason,

        @Size(max = 2000, message = "Details too long")
        String details,
        UUID reporterId
) {}