package com.fc.postservice.dto;

import java.util.UUID;

/**
 * DTO representing a single moderation audit record.

 * This record provides a read-only view of:
 *  - Which admin performed an action
 *  - What action was taken (REMOVE, APPROVE, RESTORE, etc.)
 *  - Which post or report the action applied to
 *  - Timestamp of when the action occurred

 * Used primarily in:
 *  - Admin moderation history page
 *  - Moderation audit logs
 *  - Report resolution tracking
 */
public record ModerationAuditDto(
        /*
         * Unique identifier for this moderation audit entry.
         */
        UUID id,

        /*
         * ID of the associated report, if applicable.
         * Can be null when the action did not originate from a user report.
         */
        String reportId,

        /*
         * ID of the post involved in the moderation action.
         */
        String postId,

        /*
         * ID of the admin or moderator who performed the action.
         */
        String actionBy,

        /*
         * The type of action performed.
         * Examples:
         *  - "REMOVE"
         *  - "APPROVE"
         *  - "RESTORE"
         *  - "FLAG"
         */
        String actionType,

        /*
         * Optional explanation added by the moderator (e.g., reason for removal).
         */
        String actionReason,

        /*
         * Timestamp (ISO format) when the moderation action occurred.
         */
        String createdAt
) {}
