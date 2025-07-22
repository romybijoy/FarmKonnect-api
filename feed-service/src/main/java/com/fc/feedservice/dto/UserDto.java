package com.fc.feedservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID userId;
    private String name;
    private String description;
    private String profileImage;
    private String district;
}
