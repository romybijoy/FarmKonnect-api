package com.fc.postservice.service;

import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.model.Report;
import com.fc.postservice.enums.ReportStatus;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.ReportRepository;
import com.fc.postservice.messaging.KafkaPublisher;
import com.fc.postservice.specification.ReportSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final ModerationAuditService auditService;
    private final KafkaPublisher kafkaPublisher;

    public ReportService(ReportRepository reportRepository,
                         PostRepository postRepository,
                         ModerationAuditService auditService,
                         KafkaPublisher kafkaPublisher) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.auditService = auditService;
        this.kafkaPublisher = kafkaPublisher;
    }

    public Report createReport(UUID postId, UUID reporterId, String reason, String details) {
        // dedupe: avoid duplicate PENDING reports by same reporter for same post - optional
        Report r = new Report();
        r.setPostId(postId);
        r.setReporterId(reporterId);
        r.setReason(reason);
        r.setDetails(details);
        r.setCreatedAt(Instant.now());
        r.setStatus(ReportStatus.PENDING);
        r = reportRepository.save(r);

        // publish reports.created for admins/notification service
        kafkaPublisher.publish("reports.created", Map.of(
                "reportId", r.getId(),
                "postId", r.getPostId(),
                "reason", r.getReason(),
                "reporterId", r.getReporterId(),
                "createdAt", r.getCreatedAt().toString()
        ));
        return r;
    }

    @Transactional
    public Report reviewReport(UUID reportId, UUID adminId, String action, String actionReason) {
        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        if (r.getStatus() != ReportStatus.PENDING) {
            // idempotency: return existing state or throw; here we throw
            throw new IllegalStateException("Report already reviewed");
        }

        if ("REMOVE_POST".equalsIgnoreCase(action)) {
            // soft delete / flag the post
            postRepository.updateStatus(r.getPostId(), PostStatus.REMOVED, adminId, actionReason);
            r.setStatus(ReportStatus.ACTIONED);

            // audit & kafka
            auditService.record(r.getId(), r.getPostId(), adminId, "REMOVE_POST", actionReason);

            kafkaPublisher.publish("posts.moderated", Map.of(
                    "postId", r.getPostId(),
                    "action", "REMOVED",
                    "reason", actionReason,
                    "moderatedBy", adminId.toString(),
                    "timestamp", Instant.now().toString()
            ));
        } else if ("DISMISS".equalsIgnoreCase(action) || "REJECT".equalsIgnoreCase(action)) {
            r.setStatus(ReportStatus.DISMISSED);
            auditService.record(r.getId(), r.getPostId(), adminId, "DISMISS_REPORT", actionReason);

            kafkaPublisher.publish("reports.reviewed", Map.of(
                    "reportId", r.getId(),
                    "status", "DISMISSED",
                    "reviewedBy", adminId,
                    "reason", actionReason,
                    "timestamp", Instant.now().toString()
            ));
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }

        r.setAdminId(adminId);
        r.setReviewedAt(Instant.now());
        reportRepository.save(r);

        // publish a reports.reviewed event for notification / admin UIs
        kafkaPublisher.publish("reports.reviewed", Map.of(
                "reportId", r.getId(),
                "postId", r.getPostId(),
                "status", r.getStatus().name(),
                "reviewedBy", adminId.toString(),
                "reviewedAt", r.getReviewedAt().toString()
        ));

        return r;
    }

    // helper: findByStatus, findById etc - implement as needed

    public Page<Report> findByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable);
    }

    public Optional<Report> findById(UUID id) {
        return reportRepository.findById(id);
    }

    public List<Report> findByPostId(UUID postId) {
        return reportRepository.findByPostId(postId);
    }

    public long countByStatus(ReportStatus status) {
        return reportRepository.countByStatus(status);
    }

    public Page<Report> findByCreatedDateFilter(String filter,
                                                Optional<Instant> fromOpt,
                                                Optional<Instant> toOpt,
                                                Pageable pageable,
                                                ZoneId zone) {
        // compute range based on filter
        Instant now = Instant.now();
        Instant from = null;
        Instant to = null;

        switch (filter != null ? filter.toLowerCase() : "") {
            case "daily":
                // today in given zone: from startOfDay to startOfNextDay
                LocalDate today = LocalDate.now(zone);
                from = today.atStartOfDay(zone).toInstant();
                to = today.plusDays(1).atStartOfDay(zone).toInstant();
                break;
            case "weekly":
                // last 7 days including today: from (today-6) 00:00 to next day 00:00
                LocalDate endDay = LocalDate.now(zone).plusDays(1);
                from = LocalDate.now(zone).minusDays(6).atStartOfDay(zone).toInstant();
                to = endDay.atStartOfDay(zone).toInstant();
                break;
            case "monthly":
                // last 30 days
                from = LocalDate.now(zone).minusDays(29).atStartOfDay(zone).toInstant();
                to = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
                break;
            case "custom":
            case "from-to":
                if (fromOpt.isPresent() || toOpt.isPresent()) {
                    from = fromOpt.orElse(null);
                    to = toOpt.orElse(null);
                }
                break;
            default:
                // no filter -> return all
                break;
        }

        // If caller passed explicit from/to (higher priority for 'custom'):
        if (fromOpt.isPresent()) from = fromOpt.get();
        if (toOpt.isPresent()) to = toOpt.get();

        Specification<Report> spec = null;
        if (from != null || to != null) {
            spec = ReportSpecification.createdBetween(from, to);
        }

        return reportRepository.findAll(spec, pageable);
    }

}
