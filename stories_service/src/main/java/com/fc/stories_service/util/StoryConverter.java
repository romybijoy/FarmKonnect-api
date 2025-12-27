package com.fc.stories_service.util;

import com.fc.stories_service.dto.StoryDto;
import com.fc.stories_service.model.Story;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class responsible for converting Story entities into StoryDto objects.
 * Handles type-specific mapping and relative timestamp formatting.
 */
@Component
@Slf4j
public class StoryConverter {

    /**
     * Converts a Story entity to a StoryDto.
     *
     * @param story Story entity from the database
     * @return mapped StoryDto
     */
    public StoryDto toDto(Story story) {

        if (story == null) {
            log.warn("Attempted to convert null Story entity into StoryDto");
            return null;
        }

        log.debug("Converting Story entity to DTO for storyId={}", story.getId());

        StoryDto dto = new StoryDto();
        dto.setId(story.getId());
        dto.setType(story.getType());

        // Map media type based on story type
        if ("image".equalsIgnoreCase(story.getType())) {
            dto.setImageUrl(story.getImageUrl());
        } else if ("video".equalsIgnoreCase(story.getType())) {
            dto.setVideoUrl(story.getVideoUrl());
        }
        // Friendly timestamp ("2 hr ago")
        dto.setTimestamp(getRelativeTime(story.getCreatedAt()));

        return dto;
    }

    /**
     * Converts a LocalDateTime into a readable "time ago" format.
     *
     * @param createdAt timestamp when the story was created
     * @return formatted time (e.g., "5 min ago", "2 hr ago")
     */
    private static String getRelativeTime(LocalDateTime createdAt) {

        if (createdAt == null) {
            return "";
        }

        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        if (duration.toMinutes() < 1) return "Just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + " min ago";
        if (duration.toHours() < 24) return duration.toHours() + " hr ago";

        return duration.toDays() + " days ago";
    }
}
