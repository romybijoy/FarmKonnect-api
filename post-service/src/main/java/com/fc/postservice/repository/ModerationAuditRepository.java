package com.fc.postservice.repository;

import com.fc.postservice.model.ModerationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

/**
 * Repository for moderation audit logs.
 * Tracks actions taken on posts or reports by moderators/admins.
 * Supports paginated search for audit history.
 */
public interface ModerationAuditRepository extends JpaRepository<ModerationAudit, UUID> {

    /**
     * Fetch paginated audit logs for a specific post.
     *
     * @param postId   ID of the post being moderated.
     * @param pageable Pagination info (page number, size, sort).
     */
    Page<ModerationAudit> findByPostId(UUID postId, Pageable pageable);

    /**
     * Fetch paginated audit logs for a specific report.
     *
     * @param reportId ID of the report moderated.
     * @param pageable Pagination info.
     */
    Page<ModerationAudit> findByReportId(UUID reportId, Pageable pageable);

    /**
     * Fetch paginated logs for actions taken by a specific admin/moderator.
     *
     * @param actionBy Admin/moderator user ID.
     * @param pageable Pagination info.
     */
    Page<ModerationAudit> findByActionBy(UUID actionBy, Pageable pageable);

    /**
     * Fetch audit logs based on moderation action type.
     * (Examples: "REMOVE_POST", "APPROVE_REPORT", "REJECT_REPORT")
     *
     * @param actionType String representation of the action.
     * @param pageable   Pagination info.
     */
    Page<ModerationAudit> findByActionType(String actionType, Pageable pageable);

}
