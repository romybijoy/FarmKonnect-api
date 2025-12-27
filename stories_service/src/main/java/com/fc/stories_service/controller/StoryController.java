package com.fc.stories_service.controller;

import com.fc.stories_service.dto.StoryDto;
import com.fc.stories_service.dto.StoryResponse;
import com.fc.stories_service.model.Story;
import com.fc.stories_service.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing user stories.
 * Handles creation, retrieval, and fetching stories from followed users.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stories")
@Tag(name = "Stories API", description = "Operations related to creating and fetching stories")
public class StoryController {

    private final StoryService storyService;

    /**
     * Create a new story for a user.
     * @param story the incoming story data (image/video URL, userId, etc.)
     * @return the created Story entity
     */
    @Operation(
            summary = "Create a new story",
            description = "Upload a story for a user. Stories auto-expire after 24 hours.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Story uploaded successfully",
                            content = @Content(schema = @Schema(implementation = Story.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid story data",
                            content = @Content
                    )
            }
    )
    @PostMapping("/create")
    public ResponseEntity<Story> uploadStory(@RequestBody StoryDto story) {
        log.info("Received request to create story for userId={}", story.getUserId());
        Story createdStory = storyService.createStory(story);
        log.info("Story created successfully with id={} for userId={}", createdStory.getId(), story.getUserId());
        return ResponseEntity.ok(createdStory);
    }

    /**
     * Get all active (non-expired) stories of a specific user.
     * @param userId unique identifier of the user
     * @return StoryResponse containing list of user's active stories
     */
    @Operation(
            summary = "Get stories of a specific user",
            description = "Fetch all active (not expired) stories posted by a specific user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Stories fetched successfully",
                            content = @Content(schema = @Schema(implementation = StoryResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User or stories not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<StoryResponse> getUserStories(@PathVariable UUID userId) {
        log.info("Fetching user stories for userId={}", userId);
        StoryResponse response = storyService.getUserStoriesByUserId(userId);
        log.info("Found {} active stories for userId={}", response.getStories().size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch all active stories (stories visible globally).
     * @return list of StoryResponse objects
     */
    @Operation(
            summary = "Get all active stories",
            description = "Fetch all stories that are currently active (not expired).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Active stories returned",
                            content = @Content(schema = @Schema(implementation = StoryResponse.class))
                    )
            }
    )
    @GetMapping("/active")
    public ResponseEntity<List<StoryResponse>> getAllActiveStories() {
        log.info("Fetching all active stories");
        List<StoryResponse> responses = storyService.getAllActiveStories();
        log.info("Total active stories found: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Fetch stories from the user and people they follow.
     * @param userId the user requesting the stories
     * @return list of story responses from the user + following users
     */
    @Operation(
            summary = "Get stories from people the user follows",
            description = "Returns active stories posted by the user's followings, including their own.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Stories from following users",
                            content = @Content(schema = @Schema(implementation = StoryResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/following/{userId}")
    public ResponseEntity<List<StoryResponse>> getFollowingStories(@PathVariable UUID userId) {
        log.info("Fetching following stories for userId={}", userId);
        List<StoryResponse> responses = storyService.getStoriesForUserAndFollowing(userId);
        log.info("Found {} stories for userId={} (self + following)", responses.size(), userId);
        return ResponseEntity.ok(responses);
    }

}