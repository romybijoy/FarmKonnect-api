package com.fc.postservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * PostStatsDto
 *
 * @author Romyb
 * @since 25/02/2026
 */
@Data
@AllArgsConstructor
public class PostStatsDto {
    private Long totalPosts;
    private Long pendingReports;
    private Long pendingAppeals;
}
