package com.fc.postservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/**
 * Lightweight DTO containing basic user profile information.
 * This DTO is commonly used in:
 *  - Post creation (e.g., repost metadata)
 *  - Comment and feed rendering
 *  - Notification display
 * Note:
 *  - This DTO intentionally contains only minimal fields
 *    to reduce payload size in microservice communication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @Schema(description = "Name of the user")
    private String name;

    @Schema(description = "Bio or short profile description of the user")
    private String description;

    @Schema(description = "Profile image URL of the user")
    private String profileImage;

    @Schema(description = "District or region of the user")
    private String district;
}
