package com.fc.postservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Update Comment Request DTO
 *
 * @author Romyb
 * @since 20/02/2026
 */


@Getter
@Setter
@Schema(description = "Request body for updating a comment")
public class UpdateCommentRequest {

    @NotBlank(message = "Comment content must not be empty")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    @Schema(
            description = "Updated comment content",
            example = "This is the updated comment text."
    )
    private String content;
}