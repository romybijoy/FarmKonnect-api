package com.fc.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing the block status of a user.
 * Used when checking whether a user account is blocked and
 * to return a user-friendly message along with the status.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
public class BlockStatusResponse {

    /** Indicates whether the user account is blocked */
    private boolean blocked;

    /** Additional message describing the block status */
    private String message;

    public BlockStatusResponse(boolean blocked, String message) {
        this.blocked = blocked;
        this.message = message;
    }

    // Getters and setters
}

