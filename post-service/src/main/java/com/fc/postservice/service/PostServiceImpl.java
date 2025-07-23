package com.fc.postservice.service;

import com.fc.postservice.model.PostLike;
import com.fc.postservice.model.PostLikeId;
import com.fc.postservice.repository.PostLikeRepository;
import com.postservice.*;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class PostServiceImpl extends PostServiceGrpc.PostServiceImplBase {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    public void getPostsByUserIds(UserIdsRequest request, StreamObserver<PostListResponse> responseObserver) {
        List<UUID> userIds = request.getUserIdsList().stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<Post> posts = postRepository.findByUserIdInOrderByCreatedAtDesc(userIds);

        List<PostMessage> grpcPosts = posts.stream().map(post ->
                PostMessage.newBuilder()
                        .setId(post.getId().toString())
                        .setUserId(post.getUserId().toString())
                        .setImageUrl(post.getPostImage())
                        .setProfilePic(post.getImage())
                        .setContent(post.getContent())
                        .setUserName(post.getUserName())
                        .setCreatedAt(post.getCreatedAt().toString()) // You may format date as needed
                        .build()
        ).collect(Collectors.toList());

        PostListResponse response = PostListResponse.newBuilder()
                .addAllPosts(grpcPosts)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void likePost(LikePostRequest request, StreamObserver<LikePostResponse> responseObserver) {
        try {
            UUID postId = UUID.fromString(request.getPostId());
            UUID userId = UUID.fromString(request.getUserId());

            // Ensure post exists
            postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            boolean liked;

            if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
                postLikeRepository.deleteByPostIdAndUserId(postId, userId);
                liked = false;
            } else {
                PostLike like = new PostLike();
                like.setPostId(postId);
                like.setUserId(userId);
                postLikeRepository.save(like);
                liked = true;
            }

            long likeCount = postLikeRepository.countByPostId(postId);

            LikePostResponse response = LikePostResponse.newBuilder()
                    .setLiked(liked)
                    .setLikeCount((int) likeCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error in likePost: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }




}
