package com.fc.stories_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Response object representing a group of stories belonging to a specific user.
 * Used when returning:
 * - a single user's stories
 * - story groups from following users
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoryResponse {
    /**
     * ID of the user who owns the stories.
     */
    private UUID userId;

    /**
     * Display name of the story owner.
     */
    private String userName;

    /**
     * Profile picture URL of the story owner.
     */
    private String profilePic;

    /**
     * List of the user's active (non-expired) stories.
     */
    private List<StoryDto> stories;
}
