package com.fc.feedservice.service;

import com.fc.feedservice.client.FollowServiceClient;
import com.fc.feedservice.client.PostServiceClient;
import com.fc.feedservice.client.UserServiceClient;
import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.dto.UserDto;
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
                .map(post -> mapToDto(post, userId))
                .collect(Collectors.toList());
    }

    private PostDto mapToDto(PostMessage post, UUID currentUserId) {
        // Fetch user info via gRPC (you need to implement this part)
        UserDto user = userServiceClient.getUserById(UUID.fromString(post.getUserId()));

        boolean likedByCurrentUser = postServiceClient.isPostLikedByUser(currentUserId, UUID.fromString(post.getId()));
        int likeCount = postServiceClient.getLikeCount(UUID.fromString(post.getId()));
        boolean savedByCurrentUser = postServiceClient.isPostSavedByUser(currentUserId, UUID.fromString(post.getId()));

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

                .likedByCurrentUser(likedByCurrentUser)
                .likeCount(likeCount)
                .savedByCurrentUser(savedByCurrentUser)
                .build();
    }


    public UUID repost(UUID userId, UUID originalPostId) {
       return postServiceClient.repost(userId,originalPostId);
    }

}
