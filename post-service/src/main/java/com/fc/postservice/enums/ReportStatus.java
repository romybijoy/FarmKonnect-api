package com.fc.postservice.enums;

/**
 * Represents the moderation lifecycle status of a user-submitted report.
 * Used in:
 *  - Report entity
 *  - Admin review workflows
 *  - Moderation audit logs
 *  - Filtering reports in admin dashboards
 */
public enum ReportStatus {

    /**
     * The report has been submitted by a user and is awaiting admin review.
     */
    PENDING,

    /**
     * The admin reviewed the report and concluded
     * that no action is needed on the post.
     */
    DISMISSED,

    /**
     * The admin took action on the post (e.g., removed or restricted it).
     */
    ACTIONED
}