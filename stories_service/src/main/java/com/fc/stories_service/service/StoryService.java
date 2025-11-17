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
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    private final StoryRepository storyRepository;
    private final StoryConverter storyConverter;
    private final FollowServiceClient followClient;

    public StoryResponse getUserStoriesByUserId(UUID userId) {
        List<Story> stories = storyRepository.findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());

        List<StoryDto> storyDtos = new ArrayList<>(stories.stream()
                .map(storyConverter::toDto)
                .toList());

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId != null ? userId.toString() : "")
                .build();
        UserResponse user = userStub.getUserById(request);
        return StoryResponse.builder()
                .userName(user.getUserName())
                .profilePic(user.getImage())
                .userId(userId)
                .stories(storyDtos)
                .build();

    }

    public List<StoryResponse> getAllActiveStories() {
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
            UserRequest request = UserRequest.newBuilder().setUserId(userId.toString()).build();
            UserResponse user = userStub.getUserById(request);  // gRPC call

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

        return responseList;
    }


    public Story createStory(StoryDto req) {

        UserRequest request = UserRequest.newBuilder()
                .setUserId(req.getUserId() != null ? req.getUserId().toString() : "")
                .build();
        Story story=new Story();
        UserResponse user = userStub.getUserById(request);
        story.setUsername(user.getUserName());
        story.setProfilePic(user.getImage());
        story.setType(req.getType());
        story.setImageUrl(req.getImageUrl());
        story.setVideoUrl(req.getVideoUrl());
        story.setUserId(req.getUserId());
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusHours(24));
        return storyRepository.save(story);
    }

    public List<StoryResponse> getStoriesForUserAndFollowing(UUID userId) {
        List<UUID> followedIds = new ArrayList<>(followClient.getFollowedUserIds(userId));
        // Include the logged-in user
        if (!followedIds.contains(userId)) {
            followedIds.add(userId);
        }

        List<Story> stories = storyRepository.findByUserIdInAndExpiresAtAfter(followedIds, LocalDateTime.now());

        Map<UUID, List<Story>> groupedStories = stories.stream()
                .collect(Collectors.groupingBy(Story::getUserId));

        return groupedStories.values().stream().map(userStories -> {
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
    }

    private String getTimeAgo(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 60) return minutes + " min ago";
        long hours = duration.toHours();
        if (hours < 24) return hours + " hr ago";
        return duration.toDays() + " days ago";
    }


}

