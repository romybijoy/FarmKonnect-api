package com.fc.postservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Reaction request DTO
 *
 * @author Romyb
 * @since 20/02/2026
 */

@Getter
@Setter
@Schema(description = "Request body for reacting to a comment")
public class ReactionRequest {

    @NotBlank(message = "Emoji must not be empty")
    @Size(max = 10, message = "Emoji length is too long")
    @Schema(
            description = "Emoji reaction (e.g. ❤️ 😂 🔥 😮)",
            example = "❤️"
    )
    private String emoji;
}