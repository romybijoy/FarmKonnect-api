package com.fc.feedservice.service;

import com.fc.feedservice.client.FollowServiceClient;
import com.fc.feedservice.client.PostServiceClient;
import com.fc.feedservice.client.UserServiceClient;
import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.dto.UserDto;
import com.postservice.PostMessage;
import com.postservice.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FollowServiceClient followServiceClient;
    private final PostServiceClient postServiceClient;
    private final UserServiceClient userServiceClient;
    private final HiddenPostService hiddenPostService;

    public List<PostDto> getFeed(UUID userId) {
        // 1. Get followed users
        List<UUID> followedUserIds = new ArrayList<>(followServiceClient.getFollowedUserIds(userId));

        // 2. Include self
        followedUserIds.add(userId);

        // 3. Fetch posts via PostService gRPC
        List<PostMessage> grpcPosts = postServiceClient.getPostsByUserIds(followedUserIds);

        // 4. Get hidden posts for this user
        List<UUID> hiddenPostIds = hiddenPostService.getHiddenPostsForUser(userId);

        // 5. Convert to PostDto, filtering out hidden posts
        return grpcPosts.stream()
                .filter(post -> {
                    // skip hidden
                    if (hiddenPostIds.contains(UUID.fromString(post.getId()))) return false;

                    // skip removed posts -- see helper below for two common proto shapes
                    return !isRemoved(post);
                })
                .map(post -> mapToDto(post, userId))
                .toList();
    }

    private boolean isRemoved(PostMessage post) {
        return post != null && post.getStatus() == PostStatus.REMOVED;
    }

    private PostDto mapToDto(PostMessage post, UUID currentUserId) {
        // Fetch user info
        UserDto user = userServiceClient.getUserById(UUID.fromString(post.getUserId()));

        boolean likedByCurrentUser = postServiceClient.isPostLikedByUser(currentUserId, UUID.fromString(post.getId()));
        int likeCount = postServiceClient.getLikeCount(UUID.fromString(post.getId()));
        boolean savedByCurrentUser = postServiceClient.isPostSavedByUser(currentUserId, UUID.fromString(post.getId()));

        LocalDateTime createdAt = null;
        post.getCreatedAt();
        if (!post.getCreatedAt().isBlank()) {
            try {
                createdAt = LocalDateTime.parse(post.getCreatedAt());
            } catch (DateTimeParseException e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                createdAt = LocalDateTime.parse(post.getCreatedAt(), formatter);
            }
        }

        // Base DTO
        post.getOriginalPostId();
        post.getRepostedAt();
        PostDto dto = PostDto.builder()
                .id(UUID.fromString(post.getId()))
                .userId(UUID.fromString(post.getUserId()))
                .content(post.getContent())
                .postImages(new ArrayList<>(post.getImageUrlsList()))// fixed from getImageUrl()
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
                        !post.getOriginalPostId().isBlank()
                                ? UUID.fromString(post.getOriginalPostId())
                                : null)
                .repostedAt(
                        !post.getRepostedAt().isBlank()
                                ? LocalDateTime.parse(post.getRepostedAt())
                                : null)
                .build();

        // If repost, fetch original post (limit recursion)
        if (dto.isRepost() && dto.getOriginalPostId() != null) {
            PostMessage original = postServiceClient.getPostById(dto.getOriginalPostId());
            dto.setOriginalPost(mapBasePost(original)); // shallow map
        }

        return dto;
    }

    // Helper: shallow mapping for original posts
    private PostDto mapBasePost(PostMessage post) {
        UserDto user = userServiceClient.getUserById(UUID.fromString(post.getUserId()));

        return PostDto.builder()
                .id(UUID.fromString(post.getId()))
                .userId(UUID.fromString(post.getUserId()))
                .content(post.getContent())
                .postImages(new ArrayList<>(post.getImageUrlsList()))
                .createdAt(LocalDateTime.parse(post.getCreatedAt()))
                .userName(user.getName())
                .description(user.getDescription())
                .image(user.getProfileImage())
                .district(user.getDistrict())
                .build();
    }



}
