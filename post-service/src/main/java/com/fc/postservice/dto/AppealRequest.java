package com.fc.postservice.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Appeal Request
 *
 * @author Romyb
 * @since 23/02/2026
 */
@Data
public class AppealRequest {

    private UUID postId;
    private UUID userId;
    private String reason;
}