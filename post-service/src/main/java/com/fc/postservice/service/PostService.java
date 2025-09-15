package com.fc.postservice.service;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.dto.PostRequest;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {
    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    public ModelMapper modelMapper;

    public List<PostDTO> getAllPosts() {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return posts.stream().map(post -> modelMapper.map(post, PostDTO.class)).toList();
    }



    public Post createPost(PostRequest req) {
        // Call User Service via gRPC

        UUID userId = req.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
        UserResponse user = userStub.getUserById(request);
        // Create and save post with user info (denormalized)
        Post post = new Post();
        post.setContent(req.getContent());
        post.setUserId(UUID.fromString(user.getUserId()));
        post.setUserName(user.getUserName());
        post.setDescription(user.getDescription());
        post.setImage(user.getImage());
        post.setPostImage(req.getPostImage());
        post.setDistrict(user.getDistrict());
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }
}
