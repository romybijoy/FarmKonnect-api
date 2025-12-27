package com.fc.postservice.controller;

import com.fc.postservice.dto.CreateReportDto;
import com.fc.postservice.dto.ReportDto;
import com.fc.postservice.mapper.ReportMapper;
import com.fc.postservice.model.Report;
import com.fc.postservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


/**
 * Controller for handling reports on posts.
 * Allows users to report posts with a reason and optional details.
 */
@Slf4j
@RestController
@Tag(name = "Reports", description = "API for reporting inappropriate or violating posts")
public class ReportController {

    private final ReportService reportService;
    public ReportController(ReportService reportService) { this.reportService = reportService; }

    /**
     * Create a report for a post.
     *
     * @param postId the post id (UUID)
     * @param dto    the create request
     * @return 201 Created with Location header and ReportDto body
     */
    @PostMapping("/{postId}/report")
    @Operation(
            summary = "Report a post",
            description = """
                    Creates a report for the specified post.
                    The report includes:
                    - Reporter ID
                    - Reason for report (spam, abuse, etc.)
                    - Optional additional details
                    """
    )
    public ResponseEntity<ReportDto> reportPost(
            @Parameter(description = "ID of the post to report") @PathVariable UUID postId,
            @RequestBody @Valid CreateReportDto dto) {

        log.debug("Received report request for postId={} by reporterId={}", postId, dto.reporterId());

        try {
        Report created = reportService.createReport(
                postId,
                dto.reporterId(),
                dto.reason(),
                dto.details());
        ReportDto responseDto = ReportMapper.toDto(created);

        log.info("Report created: reportId={} for postId={} by reporterId={}",
                    created.getId(), postId, dto.reporterId());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (Exception e) {
            log.error("Failed to create report for postId={} reporterId={} : {}",
                    postId, dto.reporterId(), e.getMessage(), e);
            throw e;
        }
    }

}
