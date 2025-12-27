package com.fc.postservice.mapper;

import com.fc.postservice.dto.ReportDto;
import com.fc.postservice.model.Report;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

/**
 * Utility mapper for converting {@link Report} entities into {@link ReportDto}
 * objects that are returned to clients (Admin or User).
 * Responsibilities:
 *  - Convert entity fields to API-safe DTO values
 *  - Format timestamps in ISO-8601 format (UTC)
 *  - Avoid null pointer issues for optional fields
 * This mapper intentionally contains only static methods and
 * cannot be instantiated.
 */
public final class ReportMapper {

    /** Date formatter for ISO-8601 timestamps in UTC (e.g., 2024-01-10T08:42:10Z). */
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    /**
     * Private constructor prevents instantiation.
     */
    private ReportMapper() {
        // Utility class — prevent instantiation
    }

    /**
     * Converts a {@link Report} entity into a {@link ReportDto}.
     * Converts:
     *  - UUIDs → Strings where needed
     *  - Enum status → String name
     *  - Timestamp → ISO-8601 UTC string
     *
     * @param r the Report entity
     * @return mapped ReportDto instance (never null)
     */
    public static ReportDto toDto(Report r) {
        String createdAt = r.getCreatedAt() == null ? null : ISO.format(r.getCreatedAt().atOffset(ZoneOffset.UTC));
        return new ReportDto(
                r.getId(),
                r.getPostId(),  // stays UUID in DTO
                r.getReason(),
                r.getDetails(),
                r.getStatus() == null ? null : r.getStatus().name(), // safe enum conversion
                r.getReporterId() == null ? null : r.getReporterId().toString(),
                createdAt
        );
    }
}