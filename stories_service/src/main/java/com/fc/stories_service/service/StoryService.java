package com.fc.stories_service.service;

import com.fc.stories_service.repository.StoryRepository;
import com.fc.stories_service.dto.StoryDto;
import com.fc.stories_service.dto.StoryResponse;
import com.fc.stories_service.grpc.FollowServiceClient;
import com.fc.stories_service.model.Story;
import com.fc.stories_service.util.StoryConverter;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing Stories.
 * Handles:
 * - Creating a new story
 * - Fetching stories of a user
 * - Fetching active stories globally
 * - Fetching stories from user + followings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    private final StoryRepository storyRepository;
    private final StoryConverter storyConverter;
    private final FollowServiceClient followClient;

    // ------------------------------------------------------------------------
    // USER STORIES
    // ------------------------------------------------------------------------

    /**
     * Fetch all active stories for a specific user.
     */
    public StoryResponse getUserStoriesByUserId(UUID userId) {
        log.info("Fetching stories for userId={}", userId);

        List<Story> stories = storyRepository.findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());

        List<StoryDto> storyDtos = new ArrayList<>(stories.stream()
                .map(storyConverter::toDto)
                .toList());

        // Fetch User details through gRPC
        UserResponse user = fetchUserDetails(userId);

        log.info("Found {} active stories for userId={}", storyDtos.size(), userId);

        return StoryResponse.builder()
                .userName(user.getUserName())
                .profilePic(user.getImage())
                .userId(userId)
                .stories(storyDtos)
                .build();

    }

    // ------------------------------------------------------------------------
    // ALL ACTIVE STORIES
    // ------------------------------------------------------------------------

    /**
     * Fetches all stories that have not expired (system-wide).
     */
    public List<StoryResponse> getAllActiveStories() {
        log.info("Fetching all active stories");

        List<Story> stories = storyRepository.findByExpiresAtAfter(LocalDateTime.now());

        // Group stories by userId
        Map<UUID, List<Story>> grouped = stories.stream()
                .filter(s -> s.getUserId() != null) // Ignore stories without userId
                .collect(Collectors.groupingBy(Story::getUserId));

        List<StoryResponse> responseList = new ArrayList<>();

        for (Map.Entry<UUID, List<Story>> entry : grouped.entrySet()) {
            UUID userId = entry.getKey();
            List<Story> userStories = entry.getValue();

            // Fetch user details from gRPC UserService
            UserResponse user = fetchUserDetails(userId);

            // Convert stories to DTO
            List<StoryDto> storyDtos = userStories.stream()
                    .map(storyConverter::toDto)
                    .toList();

            // Build StoryResponse
            StoryResponse response = StoryResponse.builder()
                    .userName(user.getUserName())
                    .userId(userId)
                    .profilePic(user.getImage())
                    .stories(storyDtos)
                    .build();

            responseList.add(response);
        }

        log.info("Total active story groups returned: {}", responseList.size());
        return responseList;
    }


    // ------------------------------------------------------------------------
    // CREATE STORY
    // ------------------------------------------------------------------------

    /**
     * Create a new story for a user.
     */
    public Story createStory(StoryDto req) {

        log.info("Creating story for userId={}", req.getUserId());

        // Fetch user details through gRPC
        UserResponse user = fetchUserDetails(req.getUserId());

        Story story=new Story();
        story.setUsername(user.getUserName());
        story.setProfilePic(user.getImage());
        story.setType(req.getType());
        story.setImageUrl(req.getImageUrl());
        story.setVideoUrl(req.getVideoUrl());
        story.setUserId(req.getUserId());
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusHours(24));

        Story saved = storyRepository.save(story);

        log.info("Story created: storyId={} userId={}", saved.getId(), req.getUserId());
        return saved;
    }

    // ------------------------------------------------------------------------
    // USER + FOLLOWING STORIES
    // ------------------------------------------------------------------------

    /**
     * Fetch stories from the user + followings.
     */
    public List<StoryResponse> getStoriesForUserAndFollowing(UUID userId) {
        log.info("Fetching following stories for userId={}", userId);

        List<UUID> followedIds = new ArrayList<>(followClient.getFollowedUserIds(userId));
        // Include the logged-in user
        if (!followedIds.contains(userId)) {
            followedIds.add(userId);
        }

        List<Story> stories = storyRepository.findByUserIdInAndExpiresAtAfter(followedIds, LocalDateTime.now());

        Map<UUID, List<Story>> groupedStories = stories.stream()
                .collect(Collectors.groupingBy(Story::getUserId));

        List<StoryResponse> result = groupedStories.values().stream().map(userStories -> {
            Story first = userStories.getFirst();

            List<StoryDto> storyDto = userStories.stream()
                    .map(story -> {
                        StoryDto dto = new StoryDto();
                        dto.setId(story.getId());
                        dto.setUserId(story.getUserId());
                        dto.setUsername(story.getUsername());
                        dto.setProfilePic(story.getProfilePic());
                        dto.setType(story.getType());
                        dto.setImageUrl(story.getImageUrl());
                        dto.setVideoUrl(story.getVideoUrl());
                        dto.setTimestamp(getTimeAgo(story.getCreatedAt()));
                        return dto;
                    }).toList();
            // Label logged-in user's story as "My Story"
            String displayName = first.getUserId().equals(userId) ? "My Story" : first.getUsername();

            return StoryResponse.builder()
                    .userId(first.getUserId())
                    .userName(displayName)
                    .profilePic(first.getProfilePic())
                    .stories(storyDto)
                    .build();
        }).toList();

        log.info("Returning {} story groups (user + following)", result.size());

        return result;
    }

    // ------------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------------

    /**
     * Convert timestamp into "time ago" format.
     */
    private String getTimeAgo(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 60) return minutes + " min ago";
        long hours = duration.toHours();
        if (hours < 24) return hours + " hr ago";
        return duration.toDays() + " days ago";
    }

    /**
     * Fetch user details from auth-service with error handling.
     */
    private UserResponse fetchUserDetails(UUID userId) {
        try {
            UserRequest req = UserRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();

            return userStub.getUserById(req);
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId={}. Using fallback values.", userId, e);

            // Fallback for safety
            return UserResponse.newBuilder()
                    .setUserId(userId.toString())
                    .setUserName("Unknown User")
                    .setImage("")
                    .build();
        }
    }

}

