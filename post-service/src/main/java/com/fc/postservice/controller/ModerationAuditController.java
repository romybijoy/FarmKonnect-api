package com.fc.postservice.controller;

import com.fc.postservice.dto.ModerationAuditDto;
import com.fc.postservice.model.ModerationAudit;
import com.fc.postservice.service.ModerationAuditService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/admin/audits")
public class ModerationAuditController {

    private final ModerationAuditService auditService;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public ModerationAuditController(ModerationAuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * List moderation audits with optional filters.
     * Example: /admin/audits?postId=<uuid>&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ModerationAuditDto>> listAudits(
            @RequestParam(required = false) UUID postId,
            @RequestParam(required = false) UUID reportId,
            @RequestParam(required = false) UUID adminId,
            @RequestParam(required = false) String actionType,
            Pageable pageable) {

        Page<ModerationAudit> page = auditService.findByFilters(postId, reportId, adminId, actionType, pageable);

        Page<ModerationAuditDto> dtoPage = page.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get a single audit record by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModerationAuditDto> getAudit(@PathVariable UUID id) {
        return auditService.findById(id)
                .map(a -> ResponseEntity.ok(toDto(a)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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