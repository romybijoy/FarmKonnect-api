package com.fc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * UserStatsDto
 *
 * @author Romyb
 * @since 25/02/2026
 */
@Data
@AllArgsConstructor
public class UserStatsDto {

    private Long totalUsers;
    private Long activeToday;

}