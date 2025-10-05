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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FollowServiceClient followServiceClient;
    private final PostServiceClient postServiceClient;
    private final UserServiceClient userServiceClient;
    private final HiddenPostService hiddenPostService;

    public List<PostDto> getFeed(UUID userId) {
        // 1. Get followed users
        List<UUID> followedUserIds = followServiceClient.getFollowedUserIds(userId);

        // 2. Include self
        followedUserIds.add(userId);

        // 3. Fetch posts via PostService gRPC
        List<PostMessage> grpcPosts = postServiceClient.getPostsByUserIds(followedUserIds);

        // 4. Get hidden posts for this user
        List<UUID> hiddenPostIds = hiddenPostService.getHiddenPostsForUser(userId);

        // 5. Convert to PostDto, filtering out hidden posts
        return grpcPosts.stream()
                .filter(post -> !hiddenPostIds.contains(UUID.fromString(post.getId())))
                .map(post -> mapToDto(post, userId))
                .collect(Collectors.toList());
    }

    private PostDto mapToDto(PostMessage post, UUID currentUserId) {
        // Fetch user info
        UserDto user = userServiceClient.getUserById(UUID.fromString(post.getUserId()));

        boolean likedByCurrentUser = postServiceClient.isPostLikedByUser(currentUserId, UUID.fromString(post.getId()));
        int likeCount = postServiceClient.getLikeCount(UUID.fromString(post.getId()));
        boolean savedByCurrentUser = postServiceClient.isPostSavedByUser(currentUserId, UUID.fromString(post.getId()));

        LocalDateTime createdAt = null;
        if (post.getCreatedAt() != null && !post.getCreatedAt().isBlank()) {
            try {
                createdAt = LocalDateTime.parse(post.getCreatedAt());
            } catch (DateTimeParseException e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                createdAt = LocalDateTime.parse(post.getCreatedAt(), formatter);
            }
        }

        // Base DTO
        PostDto dto = PostDto.builder()
                .id(UUID.fromString(post.getId()))
                .userId(UUID.fromString(post.getUserId()))
                .content(post.getContent())
                .postImage(post.getImageUrl()) // fixed from getImageUrl()
                .createdAt(createdAt)
                .userName(user.getName())
                .description(user.getDescription())
                .image(user.getProfileImage())
                .district(user.getDistrict())
                .likedByCurrentUser(likedByCurrentUser)
                .likeCount(likeCount)
                .savedByCurrentUser(savedByCurrentUser)
                .isRepost(post.getIsRepost())
                .originalPostId(
                        post.getOriginalPostId() != null && !post.getOriginalPostId().isBlank()
                                ? UUID.fromString(post.getOriginalPostId())
                                : null)
                .repostedAt(
                        post.getRepostedAt() != null && !post.getRepostedAt().isBlank()
                                ? LocalDateTime.parse(post.getRepostedAt())
                                : null)
                .build();

        // If repost, fetch original post (limit recursion)
        if (dto.isRepost() && dto.getOriginalPostId() != null) {
            PostMessage original = postServiceClient.getPostById(dto.getOriginalPostId());
            dto.setOriginalPost(mapBasePost(original, currentUserId)); // shallow map
        }

        return dto;
    }

    // Helper: shallow mapping for original posts
    private PostDto mapBasePost(PostMessage post, UUID currentUserId) {
        UserDto user = userServiceClient.getUserById(UUID.fromString(post.getUserId()));

        return PostDto.builder()
                .id(UUID.fromString(post.getId()))
                .userId(UUID.fromString(post.getUserId()))
                .content(post.getContent())
                .postImage(post.getImageUrl())
                .createdAt(LocalDateTime.parse(post.getCreatedAt()))
                .userName(user.getName())
                .description(user.getDescription())
                .image(user.getProfileImage())
                .district(user.getDistrict())
                .build();
    }



}
