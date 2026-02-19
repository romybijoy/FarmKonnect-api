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
import java.util.Objects;
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

        log.info("Followed users from service: {}", followedUserIds);

        // 2. Include self
        followedUserIds.add(userId);

        log.info("Final user list (including self): {}", followedUserIds);

        // 3. Fetch posts via PostService gRPC
        List<PostMessage> grpcPosts = postServiceClient.getPostsByUserIds(followedUserIds);

        log.info("Posts returned from gRPC: {}", grpcPosts.size());

        // 4. Get hidden posts for this user
        List<UUID> hiddenPostIds = hiddenPostService.getHiddenPostsForUser(userId);

        log.info("Hidden posts count: {}", hiddenPostIds.size());
        log.info("Total grpc posts: {}", grpcPosts.size());
        // 5. Convert to PostDto, filtering out hidden posts
        return grpcPosts.stream()
                .filter(post -> {

                    UUID postId = UUID.fromString(post.getId());

                    // Hide manually hidden posts
                    if (hiddenPostIds.contains(postId)) return false;

                    // Hide removed posts
                    if (isRemoved(post)) return false;

                    // Show ACTIVE posts
                    if (post.getStatus() == PostStatus.ACTIVE) return true;

                    // Show PENDING only if owner
                    if (post.getStatus() == PostStatus.PENDING &&
                            post.getUserId().equals(userId.toString())) return true;

                    return false;
                })
                .map(post -> mapToDto(post, userId))
                .filter(Objects::nonNull)
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

        if (post == null) {
            return null;
        }

        UUID postId = null;
        UUID userId = null;

        try {
            postId = UUID.fromString(post.getId());
            userId = UUID.fromString(post.getUserId());
        } catch (Exception e) {
            log.error("Invalid UUID in PostMessage: {}", post.getId(), e);
            return null;
        }

        // Fetch user info
        UserDto user = userServiceClient.getUserById(userId);

        boolean likedByCurrentUser =
                postServiceClient.isPostLikedByUser(currentUserId, postId);

        int likeCount =
                postServiceClient.getLikeCount(postId);

        boolean savedByCurrentUser =
                postServiceClient.isPostSavedByUser(currentUserId, postId);

        // -------------------------
        // Safe createdAt parsing
        // -------------------------
        LocalDateTime createdAt = null;
        String createdAtStr = post.getCreatedAt();

        if (createdAtStr != null && !createdAtStr.isBlank()) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr);
            } catch (DateTimeParseException e) {
                try {
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    createdAt = LocalDateTime.parse(createdAtStr, formatter);
                } catch (Exception ignored) {
                    log.warn("Failed to parse createdAt: {}", createdAtStr);
                }
            }
        }

        // -------------------------
        // Safe repost fields
        // -------------------------
        UUID originalPostId = null;
        String originalIdStr = post.getOriginalPostId();

        if (originalIdStr != null && !originalIdStr.isBlank()) {
            try {
                originalPostId = UUID.fromString(originalIdStr);
            } catch (Exception ignored) {
                log.warn("Invalid originalPostId: {}", originalIdStr);
            }
        }

        LocalDateTime repostedAt = null;
        String repostedAtStr = post.getRepostedAt();

        if (repostedAtStr != null && !repostedAtStr.isBlank()) {
            try {
                repostedAt = LocalDateTime.parse(repostedAtStr);
            } catch (Exception ignored) {
                log.warn("Invalid repostedAt: {}", repostedAtStr);
            }
        }

        // -------------------------
        // Build DTO
        // -------------------------
        PostDto dto = PostDto.builder()
                .id(postId)
                .userId(userId)
                .content(post.getContent() == null ? "" : post.getContent())
                .postImages(new ArrayList<>(post.getImageUrlsList()))
                .createdAt(createdAt)
                .userName(user != null ? user.getName() : null)
                .description(user != null ? user.getDescription() : null)
                .image(user != null ? user.getProfileImage() : null)
                .district(user != null ? user.getDistrict() : null)
                .likedByCurrentUser(likedByCurrentUser)
                .likeCount(likeCount)
                .savedByCurrentUser(savedByCurrentUser)
                .isRepost(post.getIsRepost())
                .originalPostId(originalPostId)
                .repostedAt(repostedAt)
                .status(post.getStatus())
                .build();


        // -------------------------
        // Handle repost safely
        // -------------------------
        if (dto.isRepost() && dto.getOriginalPostId() != null) {
            try {
                PostMessage original =
                        postServiceClient.getPostById(dto.getOriginalPostId());

                if (original != null) {
                    dto.setOriginalPost(mapBasePost(original));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch original post for repost {}", dto.getId());
            }
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
                .status(post.getStatus())
                .build();
    }

    private LocalDateTime getFeedTime(PostDto post) {
        if (post.isRepost() && post.getRepostedAt() != null) {
            return post.getRepostedAt();
        }
        return post.getCreatedAt();
    }

}
