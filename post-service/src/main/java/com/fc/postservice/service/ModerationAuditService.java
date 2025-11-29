package com.fc.postservice.service;

import com.fc.postservice.model.ModerationAudit;
import com.fc.postservice.repository.ModerationAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Service
public class ModerationAuditService {

    private final ModerationAuditRepository repo;

    public ModerationAuditService(ModerationAuditRepository repo) {
        this.repo = repo;
    }

    public void record(UUID reportId, UUID postId, UUID adminId, String actionType, String reason) {
        ModerationAudit a = new ModerationAudit();
        a.setReportId(reportId);
        a.setPostId(postId);
        a.setActionBy(adminId);
        a.setActionType(actionType);
        a.setActionReason(reason);
        repo.save(a);
    }

    public Page<ModerationAudit> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<ModerationAudit> findByFilters(UUID postId, UUID reportId, UUID adminId, String actionType, Pageable pageable) {
        // Simple implementation: if no filters, return all; else delegate to repository methods.
        // For flexibility, repository can be extended with custom queries; for now we do simple cases.

        if (postId == null && reportId == null && adminId == null && (actionType == null || actionType.isBlank())) {
            return repo.findAll(pageable);
        }

        // If repository has matching methods, use them. If not, fall back to a simple filter via JPA Specification or custom query.
        // Example implementation assumes repo has appropriate methods (you may need to add them):
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

    public Optional<ModerationAudit> findById(UUID id) {
        return repo.findById(id);
    }
}