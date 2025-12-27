package com.fc.feedservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Data Transfer Object representing basic user profile information
 * used by the Feed Service and other microservices.
 */
@Data
@Builder
@Schema(description = "Represents basic user information returned via gRPC")
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