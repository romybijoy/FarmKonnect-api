package com.fc.postservice.service;

import com.fc.notification.AppealCreatedEvent;
import com.fc.notification.AppealReviewedEvent;
import com.fc.postservice.enums.AppealStatus;
import com.fc.postservice.kafka.KafkaPublisher;
import com.fc.postservice.model.Appeal;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.AppealRepository;
import com.fc.postservice.enums.PostStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Appeal Service
 *
 * @author Romyb
 * @since 23/02/2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppealService {

    private final AppealRepository appealRepository;
    private final PostRepository postRepository;
    private final KafkaPublisher kafkaPublisher;
    private final ModerationAuditService auditService;


    // ------------------------------------------------------------
    // USER → CREATE APPEAL
    // ------------------------------------------------------------
    @Transactional
    public Appeal createAppeal(UUID postId, UUID userId, String reason) {

        log.info("Creating appeal | postId={}, userId={}", postId, userId);

        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Validate ownership
        if (!post.getUserId().equals(userId)) {
            throw new SecurityException("Only post owner can appeal");
        }

        // Validate post is removed
        if (post.getStatus() != PostStatus.REMOVED) {
            throw new IllegalStateException("Only removed posts can be appealed");
        }

        // Prevent multiple pending appeals
        boolean exists = appealRepository
                .existsByPostIdAndStatus(postId, AppealStatus.PENDING);

        if (exists) {
            throw new IllegalStateException("Appeal already pending for this post");
        }

        // Validate reason
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Appeal reason cannot be empty");
        }

        // Create appeal
        Appeal appeal = new Appeal();
        appeal.setPostId(postId);
        appeal.setUserId(userId);
        appeal.setReason(reason.trim());
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setCreatedAt(Instant.now());

        appealRepository.save(appeal);

        // Publish Protobuf event
        AppealCreatedEvent event = AppealCreatedEvent.newBuilder()
                .setAppealId(appeal.getId().toString())
                .setPostId(postId.toString())
                .setUserId(userId.toString())
                .setReason(reason)
                .setCreatedAt(appeal.getCreatedAt().toEpochMilli())
                .build();

        kafkaPublisher.publish("appeals.created", event.toByteArray());

        log.info("AppealCreatedEvent published | appealId={}", appeal.getId());

        return appeal;
    }

    // ------------------------------------------------------------
    // ADMIN → REVIEW APPEAL
    // ------------------------------------------------------------
    @Transactional
    public void reviewAppeal(UUID appealId, UUID adminId, String action) {

        log.info("Reviewing appeal | appealId={}, adminId={}, action={}",
                appealId, adminId, action);

        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new IllegalArgumentException("Appeal not found"));

        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new IllegalStateException("Appeal already reviewed");
        }

        Post post = postRepository.findById(appeal.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        AppealStatus newStatus;

        if ("APPROVE".equalsIgnoreCase(action)) {

            // Restore post
            postRepository.updateStatus(
                    post.getId(),
                    PostStatus.ACTIVE,
                    adminId,
                    "Appeal approved"
            );

            newStatus = AppealStatus.APPROVED;

            auditService.record(
                    null,
                    post.getId(),
                    adminId,
                    "APPEAL_APPROVED",
                    "Appeal approved and post restored"
            );

        } else if ("REJECT".equalsIgnoreCase(action)) {

            newStatus = AppealStatus.REJECTED;

            auditService.record(
                    null,
                    post.getId(),
                    adminId,
                    "APPEAL_REJECTED",
                    "Appeal rejected"
            );

        } else {
            throw new IllegalArgumentException("Invalid action. Use APPROVE or REJECT");
        }

        appeal.setStatus(newStatus);
        appeal.setReviewedBy(adminId);
        appeal.setReviewedAt(Instant.now());

        appealRepository.save(appeal);

        // Publish event
        AppealReviewedEvent event = AppealReviewedEvent.newBuilder()
                .setAppealId(appeal.getId().toString())
                .setPostId(post.getId().toString())
                .setUserId(appeal.getUserId().toString())
                .setStatus(newStatus.name())
                .setReviewedBy(adminId.toString())
                .setReviewedAt(appeal.getReviewedAt().toEpochMilli())
                .build();

        kafkaPublisher.publish("appeals.reviewed", event.toByteArray());

        log.info("AppealReviewedEvent published | appealId={}", appeal.getId());
    }

    public Page<Appeal> getAppealsByStatus(AppealStatus status, Pageable pageable) {
        return appealRepository.findByStatus(status, pageable);
    }

    public Page<Appeal> getAppealsByUser(UUID userId, Pageable pageable) {
        return appealRepository.findByUserId(userId, pageable);
    }

    public Page<Appeal> getAllAppeals(Pageable pageable) {
        return appealRepository.findAll(pageable);
    }
}
