package com.fc.postservice.service;

import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;

import java.time.LocalDateTime;

@Service

public class PostService {
    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @Autowired
    private PostRepository postRepository;

    public Post createPost(String email, String content) {
        // Call User Service via gRPC
        UserRequest request = UserRequest.newBuilder()
                .setEmail(email != null ? email : "")
                .build();
        UserResponse user = userStub.getUserById(request);

        // Create and save post with user info (denormalized)
        Post post = new Post();
        post.setContent(content);
        post.setEmail(user.getEmail());
        post.setUserName(user.getUserName());
        post.setDescription(user.getDescription());
        post.setDistrict(user.getDistrict());
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }
}
