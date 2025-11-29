package com.fc.postservice.repository;

import com.fc.postservice.model.ModerationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ModerationAuditRepository extends JpaRepository<ModerationAudit, UUID> {

    Page<ModerationAudit> findByPostId(UUID postId, Pageable pageable);
    Page<ModerationAudit> findByReportId(UUID reportId, Pageable pageable);
    Page<ModerationAudit> findByActionBy(UUID actionBy, Pageable pageable);
    Page<ModerationAudit> findByActionType(String actionType, Pageable pageable);

}
