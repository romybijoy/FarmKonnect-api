package com.fc.feedservice.client;

import com.postservice.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PostServiceClient {

    @GrpcClient("post-service")
    private PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub;

    @CircuitBreaker(name = "postServiceCB", fallbackMethod = "fallbackGetPosts")
    public List<PostMessage> getPostsByUserIds(List<UUID> userIds) {
        UserIdsRequest request = UserIdsRequest.newBuilder()
                .addAllUserIds(userIds.stream().map(UUID::toString).collect(Collectors.toList()))
                .build();

        return postServiceBlockingStub.getPostsByUserIds(request).getPostsList();
    }

    public LikePostResponse likePost(UUID userId, UUID postId) {
        LikePostRequest request = LikePostRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.likePost(request);
    }

    public boolean toggleSavePost(UUID userId, UUID postId) {
        SavePostRequest request = SavePostRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        SavePostResponse response = postServiceBlockingStub.savePost(request);
        return response.getSaved(); // true = saved, false = unsaved
    }

    public List<PostMessage> getSavedPosts(UUID userId) {
        GetSavedPostsRequest request = GetSavedPostsRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        GetSavedPostsResponse response = postServiceBlockingStub.getSavedPosts(request);
        return response.getPostsList();
    }

    public boolean isPostLikedByUser(UUID userId, UUID postId) {
        IsPostLikedByUserRequest request = IsPostLikedByUserRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.isPostLikedByUser(request).getLiked();
    }

    public int getLikeCount(UUID postId) {
        GetLikeCountRequest request = GetLikeCountRequest.newBuilder()
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.getLikeCount(request).getLikeCount();
    }

    public boolean isPostSavedByUser(UUID userId, UUID postId) {
        IsPostSavedByUserRequest request = IsPostSavedByUserRequest.newBuilder()
                .setUserId(userId.toString())
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.isPostSavedByUser(request).getSaved();
    }

    public PostMessage getPostById(UUID postId) {
        PostIdRequest request = PostIdRequest.newBuilder()
                .setPostId(postId.toString())
                .build();

        return postServiceBlockingStub.getPostById(request);
    }


    private List<PostMessage> fallbackGetPosts(List<UUID> userIds, Throwable t) {
        System.err.println("PostService down. Using fallback. Reason: " + t.getMessage());
        return Collections.emptyList(); // return safe empty feed
    }

}
