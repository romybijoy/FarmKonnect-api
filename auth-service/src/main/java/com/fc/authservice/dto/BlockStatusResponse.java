package com.fc.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlockStatusResponse {
    private boolean blocked;
    private String message;

    public BlockStatusResponse(boolean blocked, String message) {
        this.blocked = blocked;
        this.message = message;
    }

    // Getters and setters
}

