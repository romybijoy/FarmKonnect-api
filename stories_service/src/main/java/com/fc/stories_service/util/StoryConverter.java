package com.fc.stories_service.util;

import com.fc.stories_service.dto.StoryDto;
import com.fc.stories_service.model.Story;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class StoryConverter {
    public StoryDto toDto(Story story) {
        StoryDto dto = new StoryDto();
        dto.setId(story.getId());
        dto.setType(story.getType());

        if ("image".equalsIgnoreCase(story.getType())) {
            dto.setImageUrl(story.getImageUrl());
        } else if ("video".equalsIgnoreCase(story.getType())) {
            dto.setVideoUrl(story.getVideoUrl());
        }

        dto.setTimestamp(getRelativeTime(story.getCreatedAt()));
        return dto;
    }

    private static String getRelativeTime(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        if (duration.toMinutes() < 1) return "Just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + " min ago";
        if (duration.toHours() < 24) return duration.toHours() + " hr ago";

        return duration.toDays() + " days ago";
    }
}
