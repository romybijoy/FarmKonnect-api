package com.fc.feedservice.service;

import com.fc.feedservice.client.FollowServiceClient;
import com.fc.feedservice.client.PostServiceClient;
import com.fc.feedservice.client.UserServiceClient;
import com.fc.feedservice.dto.LikeResponseDto;
import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.dto.UserDto;
import com.postservice.LikePostResponse;
import com.postservice.PostMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FollowServiceClient followServiceClient;
    private final PostServiceClient postServiceClient;
    private final UserServiceClient userServiceClient;

    public List<PostDto> getFeed(UUID userId) {
        // 1. Get followed users
        List<UUID> followedUserIds = followServiceClient.getFollowedUserIds(userId);

        // 2. Include self
        followedUserIds.add(userId);

        // 3. Fetch posts via PostService gRPC
        List<PostMessage> grpcPosts = postServiceClient.getPostsByUserIds(followedUserIds);

        // 4. Convert to PostDto
        return grpcPosts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PostDto mapToDto(PostMessage post) {
        // Fetch user info via gRPC (you need to implement this part)
        UserDto user = userServiceClient.getUserById(UUID.fromString(post.getUserId()));

        return PostDto.builder()
                .id(UUID.fromString(post.getId()))
                .userId(UUID.fromString(post.getUserId()))
                .content(post.getContent())
                .postImage(post.getImageUrl())
                .createdAt(LocalDateTime.parse(post.getCreatedAt())) // if string, else convert appropriately
                .userName(user.getName())
                .description(user.getDescription())
                .image(user.getProfileImage())
                .district(user.getDistrict())
                .build();
    }
    public LikeResponseDto likePost(UUID userId, UUID postId) {
        LikePostResponse response = postServiceClient.likePost(userId, postId);
        return new LikeResponseDto(response.getLiked(), response.getLikeCount());
    }
    public List<PostDto> getSavedPosts(UUID userId) {
        List<PostMessage> grpcPosts = postServiceClient.getSavedPosts(userId);
        return grpcPosts.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public boolean toggleSavePost(UUID userId, UUID postId) {
        return postServiceClient.toggleSavePost(userId, postId);
    }

}
