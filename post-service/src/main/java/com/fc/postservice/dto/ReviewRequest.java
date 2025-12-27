package com.fc.postservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * DTO used by admin/moderation endpoints when reviewing a user report.
 * This request captures:
 *  - The admin action to perform (e.g., REMOVE_POST, DISMISS, RESTORE)
 *  - Optional reason describing the moderator's decision
 *  - The ID of the admin taking the action
 * It is typically used in:
 *  POST /admin/posts/reports/{id}/review
 */
public record ReviewRequest(

        /*
         * The moderation action the admin is performing.
         * Examples:
         *  - "REMOVE_POST"
         *  - "DISMISS"
         *  - "RESTORE"
         */
        @NotBlank(message = "Action is required")
        String action,

        /*
         * Optional reason provided by the moderator for the action taken.
         * Useful for audit logs and transparency.
         */
        String reason,

        /*
         * ID of the admin or moderator performing the review.
         */
        UUID adminId
) {}