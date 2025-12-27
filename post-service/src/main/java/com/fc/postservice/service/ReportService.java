package com.fc.postservice.service;

import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.model.Report;
import com.fc.postservice.enums.ReportStatus;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.ReportRepository;
import com.fc.postservice.messaging.KafkaPublisher;
import com.fc.postservice.specification.ReportSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
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

    // -------------------------------------------------------------------------
    // CREATE REPORT
    // -------------------------------------------------------------------------
    /**
     * Creates a report and publishes notification events.
     */
    public Report createReport(UUID postId, UUID reporterId, String reason, String details) {

        log.info("Creating report | postId={}, reporterId={}, reason={}", postId, reporterId, reason);

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

        log.info("Report created successfully | reportId={}, postId={}", r.getId(), r.getPostId());
        return r;
    }

    // -------------------------------------------------------------------------
    // REVIEW REPORT
    // -------------------------------------------------------------------------
    /**
     * Reviews a report with actions like REMOVE_POST or DISMISS.
     */
    @Transactional
    public Report reviewReport(UUID reportId, UUID adminId, String action, String actionReason) {

        log.info("Reviewing report | reportId={}, adminId={}, action={}", reportId, adminId, action);

        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("Report not found: " + reportId));

        if (r.getStatus() != ReportStatus.PENDING) {
            log.warn("Attempt to re-review already processed report | reportId={}", reportId);
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

    // -------------------------------------------------------------------------
    // BASIC QUERIES
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // DATE FILTER QUERY (Daily, Weekly, Monthly, Custom)
    // -------------------------------------------------------------------------
    /**
     * Filters reports based on a preset or custom date range.
     */
    public Page<Report> findByCreatedDateFilter(
            String filter,
            Optional<Instant> fromOpt,
            Optional<Instant> toOpt,
            Pageable pageable,
            ZoneId zone
    ) {

        log.info("Filtering reports | filter={}, from={}, to={}", filter, fromOpt, toOpt);

        Instant from = null;
        Instant to = null;

        switch (filter != null ? filter.toLowerCase() : "") {

            case "daily" -> {
                LocalDate today = LocalDate.now(zone);
                from = today.atStartOfDay(zone).toInstant();
                to = today.plusDays(1).atStartOfDay(zone).toInstant();
            }

            case "weekly" -> {
                LocalDate start = LocalDate.now(zone).minusDays(6);
                LocalDate end = LocalDate.now(zone).plusDays(1);
                from = start.atStartOfDay(zone).toInstant();
                to = end.atStartOfDay(zone).toInstant();
            }

            case "monthly" -> {
                LocalDate start = LocalDate.now(zone).minusDays(29);
                LocalDate end = LocalDate.now(zone).plusDays(1);
                from = start.atStartOfDay(zone).toInstant();
                to = end.atStartOfDay(zone).toInstant();
            }

            case "custom", "from-to" -> {
                from = fromOpt.orElse(null);
                to = toOpt.orElse(null);
            }

            default -> {
                // no filter -> return all
                from = fromOpt.orElse(null);
                to = toOpt.orElse(null);
            }
        }

        Specification<Report> spec = null;

        if (from != null || to != null) {
            spec = ReportSpecification.createdBetween(from, to);
        }

        return reportRepository.findAll(spec, pageable);
    }

}
