package com.fc.postservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * DTO for admin review calls.
 */
public record ReviewRequest(
        @NotBlank(message = "Action is required") // e.g. REMOVE_POST or DISMISS
        String action,

        String reason,

        UUID adminId
) {}