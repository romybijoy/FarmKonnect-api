package com.fc.feedservice.client;

import com.postservice.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


/**
 * gRPC client for communicating with the Post Service.
 * This client is used by the Feed Service to:
 * - Fetch posts created by users
 * - Like and save posts
 * - Check like/save states
 * - Retrieve saved posts
 * Resilience4j's Circuit Breaker is used to prevent cascading failures
 * when the Post Service becomes unavailable.
 */
@Slf4j
@Service
@SuppressWarnings("unused")
public class PostServiceClient {

    /**
     * Injects a gRPC blocking stub for post-service using the service name
     * defined in application.yml -> grpc.client.post-service
     */
    @GrpcClient("post-service")
    private PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub;

    /**
     * Fetches posts created by a list of user IDs.
     * If the PostService is down or slow, the CircuitBreaker triggers the
     * fallback method: fallbackGetPosts().
     */
    @CircuitBreaker(name = "postServiceCB", fallbackMethod = "fallbackGetPosts")
    public List<PostMessage> getPostsByUserIds(List<UUID> userIds) {
        UserIdsRequest request = UserIdsRequest.newBuilder()
                .addAllUserIds(userIds.stream().map(UUID::toString).toList())
                .build();

        return postServiceBlockingStub.getPostsByUserIds(request).getPostsList();
    }

    /**
     * Sends a like request for a post by a user.
     */
    public LikePostResponse likePost(UUID userId, UUID postId) {
        LikePostRequest request = LikePostRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.likePost(request);
    }

    /**
     * Toggles saved status for a post by a user.
     *
     * @return true if saved, false if unsaved
     */
    public boolean toggleSavePost(UUID userId, UUID postId) {
        SavePostRequest request = SavePostRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        SavePostResponse response = postServiceBlockingStub.savePost(request);
        return response.getSaved(); // true = saved, false = unsaved
    }

    /**
     * Returns all posts saved by a user.
     */
    public List<PostMessage> getSavedPosts(UUID userId) {
        GetSavedPostsRequest request = GetSavedPostsRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        GetSavedPostsResponse response = postServiceBlockingStub.getSavedPosts(request);
        return response.getPostsList();
    }

    /**
     * Checks if a user has liked a post.
     */
    public boolean isPostLikedByUser(UUID userId, UUID postId) {
        IsPostLikedByUserRequest request = IsPostLikedByUserRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.isPostLikedByUser(request).getLiked();
    }

    /**
     * Retrieves the total like count for a post.
     */
    public int getLikeCount(UUID postId) {
        GetLikeCountRequest request = GetLikeCountRequest.newBuilder()
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.getLikeCount(request).getLikeCount();
    }

    /**
     * Checks if a user has saved a post.
     */
    public boolean isPostSavedByUser(UUID userId, UUID postId) {
        IsPostSavedByUserRequest request = IsPostSavedByUserRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.isPostSavedByUser(request).getSaved();
    }

    /**
     * Retrieves a single post by its ID.
     */
    public PostMessage getPostById(UUID postId) {
        PostIdRequest request = PostIdRequest.newBuilder()
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.getPostById(request);
    }


    /**
     * Fallback method used when CircuitBreaker detects a failure.
     * This is triggered when PostService is down, slow, or throwing errors.
     *
     * @param userIds IDs of users whose posts were requested
     * @param t       the exception that caused the fallback
     * @return empty list → ensures Feed Service still works safely
     */
    private List<PostMessage> fallbackGetPosts(List<UUID> userIds, Throwable t) {
        // Use structured logging provided by Lombok's @Slf4j instead of printing to stderr
        if (t != null) {
            log.warn("PostService down. Using fallback for userIds={} - Reason: {}", userIds, t.getMessage(), t);
        } else {
            log.warn("PostService down. Using fallback for userIds={}", userIds);
        }
        return Collections.emptyList(); // return safe empty feed
    }

}
