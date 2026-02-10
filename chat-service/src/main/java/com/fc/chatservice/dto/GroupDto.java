package com.fc.chatservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO to fetch ChatGroup based on group Id
 *
 * @author Romyb
 * @since 12/12/2025
 */
@Builder
public record GroupDto(
    UUID id,
    String name,
    UUID createdBy,
    LocalDateTime createdAt,
    List<UUID> memberIds,
    int memberCount,
    String avatar ) {
}
