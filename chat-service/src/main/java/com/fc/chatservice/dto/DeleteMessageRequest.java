package com.fc.chatservice.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Delete Request DTO
 *
 * @author Romyb
 * @since 30/12/2025
 */

@Data
public class DeleteMessageRequest {

    private UUID messageId;
    private UUID userId;
    private String deleteType;
}
