package com.fc.feedservice.service;

import com.fc.feedservice.client.FollowServiceClient;
import com.fc.feedservice.client.PostServiceClient;
import com.fc.feedservice.client.UserServiceClient;
import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.dto.UserDto;
import com.postservice.PostMessage;
import com.postservice.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for generating a personalized feed for the user.
 * Steps involved:
 * 1. Fetch followed users via FollowService (gRPC)
 * 2. Add self to feed sources
 * 3. Fetch posts from PostService (gRPC)
 * 4. Filter hidden posts
 * 5. Map PostMessage → PostDto with user details and engagement info
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FollowServiceClient followServiceClient;
    private final PostServiceClient postServiceClient;
    private final UserServiceClient userServiceClient;
    private final HiddenPostService hiddenPostService;

    /**
     * Builds the feed for a given user by fetching posts from followed users,
     * filtering hidden posts, and enriching each post with details.
     */
    public List<PostDto> getFeed(UUID userId) {

        log.debug("Generating feed for userId={}", userId);

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
                    // Hide posts user manually hid
                    if (hiddenPostIds.contains(UUID.fromString(post.getId()))) return false;

                    // Hide posts removed by the owner
                    return !isRemoved(post);
                })
                .map(post -> mapToDto(post, userId))
                .sorted((a, b) -> getFeedTime(b).compareTo(getFeedTime(a)))
                .toList();
    }

    /**
     * Checks if a post has been marked as REMOVED in the PostService.
     */
    private boolean isRemoved(PostMessage post) {
        return post != null && post.getStatus() == PostStatus.REMOVED;
    }

    /**
     * Converts a gRPC PostMessage into a PostDto including:
     * - full user details (via grpc user-service)
     * - like & save state for the current user
     * - repost details if applicable
     */
    private PostDto mapToDto(PostMessage post, UUID currentUserId) {

        // Fetch user info of original post creator
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

        // Handle repost case
        if (dto.isRepost() && dto.getOriginalPostId() != null) {
            PostMessage original = postServiceClient.getPostById(dto.getOriginalPostId());
            dto.setOriginalPost(mapBasePost(original)); // shallow map
        }

        return dto;
    }

    /**
     * Shallow mapping for original posts inside a repost.
     */
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

    private LocalDateTime getFeedTime(PostDto post) {
        if (post.isRepost() && post.getRepostedAt() != null) {
            return post.getRepostedAt();
        }
        return post.getCreatedAt();
    }

}
