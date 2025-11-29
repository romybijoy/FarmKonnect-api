package com.fc.postservice.controller;

import com.fc.postservice.dto.CreateReportDto;
import com.fc.postservice.dto.ReportDto;
import com.fc.postservice.mapper.ReportMapper;
import com.fc.postservice.model.Report;
import com.fc.postservice.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ReportController {

    // Logger for this class
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

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
    public ResponseEntity<ReportDto> reportPost(
            @PathVariable UUID postId,
            @RequestBody @Valid CreateReportDto dto) {

        Report created = reportService.createReport(postId, dto.reporterId(), dto.reason(), dto.details());
        ReportDto responseDto = ReportMapper.toDto(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

}
