package com.fc.postservice.service;

import com.fc.notification.PostModerationEvent;
import com.fc.notification.ReportCreatedEvent;
import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.model.Report;
import com.fc.postservice.enums.ReportStatus;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.ReportRepository;
import com.fc.postservice.kafka.KafkaPublisher;
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

        // Build Protobuf Event
        ReportCreatedEvent event = ReportCreatedEvent.newBuilder()
                .setReportId(r.getId().toString())
                .setPostId(r.getPostId().toString())
                .setReporterId(r.getReporterId().toString())
                .setReason(r.getReason())
                .setDetails(r.getDetails() != null ? r.getDetails() : "")
                .setCreatedAt(r.getCreatedAt().toEpochMilli())
                .build();

        // Publish as byte[]
        kafkaPublisher.publish("reports.created", event.toByteArray());

        log.info("ReportCreatedEvent published | reportId={}, postId={}",
                r.getId(), r.getPostId());

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

        UUID ownerId = postRepository.findOwnerIdByPostId(r.getPostId());

        String moderationAction;

        if ("REMOVE_POST".equalsIgnoreCase(action)) {

            postRepository.updateStatus(r.getPostId(), PostStatus.REMOVED, adminId, actionReason);
            r.setStatus(ReportStatus.ACTIONED);

            moderationAction = "REMOVED";

            auditService.record(r.getId(), r.getPostId(), adminId, "REMOVE_POST", actionReason);

        } else if ("DISMISS".equalsIgnoreCase(action) || "REJECT".equalsIgnoreCase(action)) {

            r.setStatus(ReportStatus.DISMISSED);

            moderationAction = "DISMISSED";

            auditService.record(r.getId(), r.getPostId(), adminId, "DISMISS_REPORT", actionReason);

        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }

        r.setAdminId(adminId);
        r.setReviewedAt(Instant.now());
        reportRepository.save(r);

        // Publish Protobuf moderation event
        PostModerationEvent event = PostModerationEvent.newBuilder()
                .setPostId(r.getPostId().toString())
                .setOwnerId(ownerId.toString())
                .setAction(moderationAction)
                .setReason(actionReason != null ? actionReason : "")
                .setModeratedBy(adminId.toString())
                .setTimestamp(Instant.now().toEpochMilli()) // long type for timestamp
                .build();

        kafkaPublisher.publish("posts.moderated", event.toByteArray());

        log.info("PostModerationEvent published | postId={}, ownerId={}",
                r.getPostId(), ownerId);

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
