package com.fc.postservice.controller;

import com.fc.postservice.dto.ReportDto;
import com.fc.postservice.dto.ReviewRequest;
import com.fc.postservice.dto.admin.PostDetailAdminDto;
import com.fc.postservice.dto.admin.PostResponse;
import com.fc.postservice.enums.ReportStatus;
import com.fc.postservice.model.Report;
import com.fc.postservice.service.AdminPostService;
import com.fc.postservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin controller for managing posts, including:
 * - Viewing all posts
 * - Viewing detailed post information
 * - Viewing and reviewing reports
 * - Filtering reports by date range and status
 */
@RestController
@RequestMapping("/admin/posts")
@Tag(name = "Admin Post Management", description = "Admin-only APIs for managing posts and reports")
public class AdminPostController {

    private final AdminPostService adminPostService;
    private final ReportService reportService;

    public AdminPostController(AdminPostService adminPostService, ReportService reportService) {

        this.adminPostService = adminPostService;
        this.reportService = reportService;
    }

    /**
     * Returns paginated list of posts for admin review.
     */
    @Operation(summary = "Get all posts (Admin)", description = "Returns paginated list of all posts.")
    @GetMapping
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder
    ) {
        PostResponse postsResponse = adminPostService.getAllPosts(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(postsResponse, HttpStatus.FOUND); // 200 instead of 302 (FOUND)
    }

    /**
     * Returns detailed information about a single post.
     */
    @Operation(summary = "Get post details (Admin)")
    @GetMapping("/{id}")
    public PostDetailAdminDto getPost(@PathVariable("id") UUID id) {
        try {
            return adminPostService.getPostDetail(id);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    /**
     * Returns paginated list of reports filtered by status.
     */
    @Operation(summary = "List post reports (Admin)", description = "Returns reports filtered by status.")
    @GetMapping("/reports")
    public Page<ReportDto> listReports(@RequestParam(defaultValue = "PENDING") String status,
                                       Pageable pageable) {
        return reportService.findByStatus(ReportStatus.valueOf(status), pageable)
                .map(this::toDto);
    }

    /** Converts Report entity → DTO */
    private ReportDto toDto(Report r) {
        return new ReportDto(
                r.getId(),
                r.getPostId(),
                r.getReason(),
                r.getDetails(),
                r.getStatus().name(),
                r.getReporterId().toString(),
                r.getCreatedAt().toString()
        );
    }

    /**
     * Reviews a report (APPROVE / REJECT) and updates its status.
     */
    @Operation(summary = "Review a report (Admin)")
    @PostMapping("/reports/{id}/review")
    public ResponseEntity<?> review(@PathVariable UUID id,
                                    @RequestBody ReviewRequest req) {
        Report r = reportService.reviewReport(id, req.adminId(), req.action(), req.reason());
        return ResponseEntity.ok(Map.of("id", r.getId(), "status", r.getStatus()));
    }

    /**
     * Returns reports filtered by:
     *  - text filter
     *  - date range (from/to)
     *  - timezone-aware filtering
     */
    @Operation(summary = "Filter reports by date (Admin)")
    @GetMapping("/dateReports")
    public Page<Report> getReports(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "tz", required = false) String zoneId, // optional timezone like "Asia/Kolkata"
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ZoneId zone = (zoneId != null && !zoneId.isBlank()) ? ZoneId.of(zoneId) : ZoneId.systemDefault();
        return reportService.findByCreatedDateFilter(filter,
                Optional.ofNullable(from),
                Optional.ofNullable(to),
                pageable,
                zone);
    }
}
