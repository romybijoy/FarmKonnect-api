package com.fc.postservice.mapper;

import com.fc.postservice.dto.ReportDto;
import com.fc.postservice.model.Report;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

public final class ReportMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private ReportMapper() {}

    public static ReportDto toDto(Report r) {
        String createdAt = r.getCreatedAt() == null ? null : ISO.format(r.getCreatedAt().atOffset(ZoneOffset.UTC));
        return new ReportDto(
                r.getId(),
                r.getPostId(),
                r.getReason(),
                r.getDetails(),
                r.getStatus() == null ? null : r.getStatus().name(),
                r.getReporterId() == null ? null : r.getReporterId().toString(),
                createdAt
        );
    }
}