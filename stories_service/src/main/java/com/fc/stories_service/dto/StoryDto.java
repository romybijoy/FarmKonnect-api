package com.fc.stories_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoryDto {
    private UUID id;
    private String type;
    private String email;
    private String username;
    private String profilePic;
    private String imageUrl;
    private String videoUrl;
    private String timestamp;
}
