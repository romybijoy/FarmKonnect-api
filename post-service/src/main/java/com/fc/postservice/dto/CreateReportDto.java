package com.fc.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO used when a user submits a report for a post.
 * This record captures:
 *  - The reason for reporting the post (required)
 *  - Optional additional details
 *  - The ID of the user submitting the report
 * Records are ideal for this purpose because:
 *  - They are immutable
 *  - They work seamlessly with Jackson for JSON serialization
 *  - They provide compact, readable declaration syntax
 */
public record CreateReportDto(
        /*
         * The main reason for reporting the post.
         * This field is required and limited to 100 characters.
         */
        @NotBlank(message = "Reason is required")
        @Size(max = 100, message = "Reason too long")
        String reason,

        /*
         * Optional additional explanation for the report.
         * Limited to 2000 characters.
         */
        @Size(max = 2000, message = "Details too long")
        String details,

        /*
         * The ID of the user filing the report.
         */
        UUID reporterId
) {}