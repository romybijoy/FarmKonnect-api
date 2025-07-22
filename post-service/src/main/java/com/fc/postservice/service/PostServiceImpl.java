package com.fc.postservice.service;

import com.postservice.PostServiceGrpc;
import com.postservice.PostListResponse;
import com.postservice.UserIdsRequest;
import com.postservice.PostMessage;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
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
}
