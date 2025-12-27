package com.fc.postservice.service;

import com.fc.postservice.model.ModerationAudit;
import com.fc.postservice.repository.ModerationAuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for recording and retrieving moderation audit logs.
 * Tracks admin actions performed on posts or user reports.
 */
@Service
@Slf4j
public class ModerationAuditService {

    private final ModerationAuditRepository repo;

    public ModerationAuditService(ModerationAuditRepository repo) {
        this.repo = repo;
    }

    /**
     * Records a moderation action.
     *
     * @param reportId   ID of the report (nullable)
     * @param postId     ID of the post affected
     * @param adminId    Admin/moderator performing the action
     * @param actionType Type of action ("REMOVE_POST", "RESTORE_POST", etc.)
     * @param reason     Explanation or justification for action
     */
    public void record(UUID reportId, UUID postId, UUID adminId, String actionType, String reason) {

        log.info("Recording moderation action | postId={}, reportId={}, adminId={}, type={}",
                postId, reportId, adminId, actionType);

        ModerationAudit audit = new ModerationAudit();
        audit.setReportId(reportId);
        audit.setPostId(postId);
        audit.setActionBy(adminId);
        audit.setActionType(actionType);
        audit.setActionReason(reason);
        repo.save(audit);

        log.info("Moderation audit saved successfully | id={}", audit.getId());
    }

    /**
     * Retrieves all audit entries with pagination.
     */
    public Page<ModerationAudit> findAll(Pageable pageable) {
        log.debug("Fetching all moderation audits | page={}", pageable);
        return repo.findAll(pageable);
    }

    /**
     * Fetches moderation audits based on available filters.
     * Priority-based approach:
     * 1. postId
     * 2. reportId
     * 3. adminId
     * 4. actionType
     * Only one filter is used at a time for clean endpoint behavior.
     */
    public Page<ModerationAudit> findByFilters(UUID postId, UUID reportId, UUID adminId, String actionType, Pageable pageable) {
        log.info("Filtering moderation audits | postId={}, reportId={}, adminId={}, type={}",
                postId, reportId, adminId, actionType);

        // No filters applied → return all
        if (postId == null && reportId == null && adminId == null && (actionType == null || actionType.isBlank())) {

            log.debug("No filters applied, returning all audits.");
            return repo.findAll(pageable);
        }

        // Apply filters with clean priority system
        if (postId != null) {
            return repo.findByPostId(postId, pageable);
        }
        if (reportId != null) {
            return repo.findByReportId(reportId, pageable);
        }
        if (adminId != null) {
            return repo.findByActionBy(adminId, pageable);
        }
        if (!actionType.isBlank()) {
            return repo.findByActionType(actionType, pageable);
        }

        return repo.findAll(pageable);
    }

    /**
     * Retrieve a specific audit entry by ID.
     */
    public Optional<ModerationAudit> findById(UUID id) {
        log.debug("Fetching moderation audit by id={}", id);
        return repo.findById(id);
    }
}