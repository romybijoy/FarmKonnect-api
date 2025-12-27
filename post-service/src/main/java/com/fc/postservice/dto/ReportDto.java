package com.fc.postservice.dto;

import java.util.UUID;

/**
 * DTO returned to the frontend representing a user-submitted report.
 * This is the API-facing version of the Report entity, containing:
 *  - Basic report information
 *  - Reporting user details
 *  - Post being reported
 *  - Moderation status
 *  - ISO-formatted timestamp
 * Notes:
 *  - The createdAt field is returned as an ISO-8601 formatted String.
 *  - Mapping from entity → DTO should be done via MapStruct or a manual mapper.
 */
public record ReportDto(

        /*
         * Unique ID of the report.
         */
        UUID id,

        /*
         * ID of the post that is being reported.
         */
        UUID postId,

        /*
         * The reason selected by the user (e.g., "HATE_SPEECH", "SPAM").
         */
        String reason,

        /*
         * Optional additional explanation entered by the user.
         */
        String details,

        /*
         * Current moderation status.
         * Possible values: PENDING, APPROVED, REJECTED.
         */
        String status,

        /*
         * ID of the user who submitted the report.
         */
        String reporterId,

        /*
         * ISO-8601 formatted timestamp when the report was created.
         * Example: "2024-01-15T13:42:00Z"
         */
        String createdAt
) {}