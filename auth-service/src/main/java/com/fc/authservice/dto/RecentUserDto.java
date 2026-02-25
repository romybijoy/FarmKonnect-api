package com.fc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Recent User Dto
 *
 * @author Romyb
 * @since 25/02/2026
 */
@Data
@AllArgsConstructor
public class RecentUserDto {

    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

}