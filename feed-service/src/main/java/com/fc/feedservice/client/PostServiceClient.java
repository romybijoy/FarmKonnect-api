package com.fc.feedservice.client;

import com.postservice.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.postservice.*;


@Slf4j
@Service
public class PostServiceClient {

    private final PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub;

    public PostServiceClient() {
        // Ensure this matches your PostService gRPC host and port
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9095)
                .usePlaintext()
                .build();

        postServiceBlockingStub = PostServiceGrpc.newBlockingStub(channel);
    }

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

    public UUID repost(UUID userId, UUID originalPostId) {
        RepostRequest request = RepostRequest.newBuilder()
                .setOriginalPostId(originalPostId.toString())
                .setRepostedBy(userId.toString())
                .build();

        RepostResponse response = postServiceBlockingStub.repost(request);
        return UUID.fromString(response.getNewPostId());
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

}
