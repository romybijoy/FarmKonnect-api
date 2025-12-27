package com.fc.postservice.controller;

import com.fc.postservice.dto.ModerationAuditDto;
import com.fc.postservice.model.ModerationAudit;
import com.fc.postservice.service.ModerationAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Controller responsible for exposing moderation audit logs for admin users.
 * Supports filtering by:
 *  - post ID
 *  - report ID
 *  - admin ID
 *  - action type (e.g., REMOVE, APPROVE)
 * Provides APIs to:
 *  - List moderation audit records with pagination
 *  - Fetch details of a specific moderation audit
 */
@Slf4j
@RestController
@RequestMapping("/admin/audits")
@Tag(name = "Moderation Audits", description = "Admin endpoints for viewing moderation audit logs")
public class ModerationAuditController {

    private final ModerationAuditService auditService;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public ModerationAuditController(ModerationAuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Lists moderation audit records filtered by optional criteria.
     */
    @Operation(
            summary = "List moderation audits",
            description = """
                    Fetches paginated moderation audit logs.
                    Optional filters:
                    - postId
                    - reportId
                    - adminId
                    - actionType
                    """
    )
    @GetMapping
    public ResponseEntity<Page<ModerationAuditDto>> listAudits(
            @RequestParam(required = false) UUID postId,
            @RequestParam(required = false) UUID reportId,
            @RequestParam(required = false) UUID adminId,
            @RequestParam(required = false) String actionType,
            Pageable pageable) {

        log.debug("Listing audits with filters postId={}, reportId={}, adminId={}, actionType={}, page={}, size={}",
                postId, reportId, adminId, actionType, pageable.getPageNumber(), pageable.getPageSize());

        try {
        Page<ModerationAudit> page = auditService.findByFilters(postId, reportId, adminId, actionType, pageable);

        Page<ModerationAuditDto> dtoPage = page.map(this::toDto);
            log.info("Fetched {} audit records (page {}/{})",
                    dtoPage.getNumberOfElements(),
                    dtoPage.getNumber() + 1,
                    dtoPage.getTotalPages()
            );
        return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            log.error("Error while listing audit logs: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns details of a moderation audit by ID.
     */
    @Operation(
            summary = "Get moderation audit by ID",
            description = "Fetch a single moderation audit record using its unique identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ModerationAuditDto> getAudit( @Parameter(description = "Audit record ID") @PathVariable UUID id) {
        log.debug("Fetching audit record with id={}", id);

        try {
        return auditService.findById(id)
                .map(audit -> {
                    log.info("Audit record found: id={}", id);
                    return ResponseEntity.ok(toDto(audit));
                })
                .orElseGet(() -> {
                    log.warn("Audit record not found: id={}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Failed to fetch audit record id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converts the ModerationAudit entity into a DTO for API responses.
     */
    private ModerationAuditDto toDto(ModerationAudit a) {
        return new ModerationAuditDto(
                a.getId(),
                a.getReportId() == null ? null : a.getReportId().toString(),
                a.getPostId() == null ? null : a.getPostId().toString(),
                a.getActionBy() == null ? null : a.getActionBy().toString(),
                a.getActionType(),
                a.getActionReason(),
                a.getCreatedAt() == null ? null : ISO.format(a.getCreatedAt())
        );
    }

}