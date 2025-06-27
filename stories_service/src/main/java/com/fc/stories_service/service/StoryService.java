package com.fc.stories_service.service;

import com.fc.stories_service.Repository.StoryRepository;
import com.fc.stories_service.dto.StoryDto;
import com.fc.stories_service.dto.StoryResponse;
import com.fc.stories_service.model.Story;
import com.fc.stories_service.util.StoryConverter;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    private final StoryRepository storyRepository;
    private final StoryConverter storyConverter;

    public StoryResponse getUserStoriesByEmail(String email) {
        List<Story> stories = storyRepository.findByEmailAndExpiresAtAfter(email, LocalDateTime.now());

        List<StoryDto> storyDtos = stories.stream()
                .map(storyConverter::toDto)
                .collect(Collectors.toList());

        UserRequest request = UserRequest.newBuilder()
                .setEmail(email != null ? email : "")
                .build();
        UserResponse user = userStub.getUserById(request);
        return StoryResponse.builder()
                .userName(user.getUserName())
                .profilePic(user.getImage())
                .email(email)
                .stories(storyDtos)
                .build();

    }

    public List<StoryResponse> getAllActiveStories() {
        List<Story> stories = storyRepository.findByExpiresAtAfter(LocalDateTime.now());

        // Group stories by email
        Map<String, List<Story>> grouped = stories.stream()
                .filter(s -> s.getEmail() != null) // Ignore stories without email
                .collect(Collectors.groupingBy(Story::getEmail));

        List<StoryResponse> responseList = new ArrayList<>();

        for (Map.Entry<String, List<Story>> entry : grouped.entrySet()) {
            String email = entry.getKey();
            List<Story> userStories = entry.getValue();

            // Fetch user details from gRPC UserService
            UserRequest request = UserRequest.newBuilder().setEmail(email).build();
            UserResponse user = userStub.getUserById(request);  // gRPC call

            // Convert stories to DTO
            List<StoryDto> storyDtos = userStories.stream()
                    .map(storyConverter::toDto)
                    .collect(Collectors.toList());

            // Build StoryResponse
            StoryResponse response = StoryResponse.builder()
                    .userName(user.getUserName())
                    .email(email)
                    .profilePic(user.getImage())
                    .stories(storyDtos)
                    .build();

            responseList.add(response);
        }

        return responseList;
    }


    public Story createStory(StoryDto req) {

        UserRequest request = UserRequest.newBuilder()
                .setEmail(req.getEmail() != null ? req.getEmail() : "")
                .build();
        Story story=new Story();
        UserResponse user = userStub.getUserById(request);
        story.setUsername(user.getUserName());
        story.setProfilePic(user.getImage());
        story.setType(req.getType());
        story.setImageUrl(req.getImageUrl());
        story.setVideoUrl(req.getVideoUrl());
        story.setEmail(req.getEmail());
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusHours(24));
        return storyRepository.save(story);
    }

//    public List<StoryDto> getUserStoryDtos(UUID userId) {
//        List<Story> stories = storyRepository.findByEmailAndExpiresAtAfter(ema, LocalDateTime.now());
//        return stories.stream()
//                .map(StoryConverter::toDto)
//                .collect(Collectors.toList());
//    }

}

