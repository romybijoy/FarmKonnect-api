package com.fc.postservice.service;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.dto.PostRequest;
import com.fc.postservice.dto.UpdatePostRequest;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
import com.postservice.PostMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing posts including:
 * - Creating posts
 * - Reposting
 * - Fetching posts (REST + gRPC conversions)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    public ModelMapper modelMapper;

    /**
     * Get all posts sorted by creation date DESC.
     */
    public List<PostDTO> getAllPosts() {
        log.info("Fetching all posts sorted by createdAt DESC");

        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return posts.stream()
                .map(post -> modelMapper.map(post, PostDTO.class))
                .toList();
    }

    /**
     * Fetches posts created by a specific user.
     *
     * @param userId User ID whose posts are requested
     * @param page   Page number (zero-based)
     * @param size   Number of posts per page
     * @return Paginated list of posts
     */
    public Page<Post> getPostsByUserId(UUID userId, int page, int size) {

        log.info("Fetching posts for userId={}, page={}, size={}", userId, page, size);

        Page<Post> posts = postRepository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );

        log.info("Found {} posts for userId={}", posts.getTotalElements(), userId);

        return posts;
    }


    /**
     * Fetch posts for feed service in gRPC format (with images).
     */
    @Transactional(readOnly = true)
    public List<PostMessage> getPostsByUserIdsAsGrpc(List<UUID> userIds) {
        log.info("Fetching posts via gRPC for userIds={}", userIds);

        List<Post> posts = postRepository.findByUserIdInWithImages(userIds);
        return posts.stream()
                .map(this::toGrpcPost)
                .collect(Collectors.toList());
    }

    /**
     * Convert Post -> gRPC PostMessage.
     */
    private PostMessage toGrpcPost(Post post) {
        PostMessage.Builder builder = PostMessage.newBuilder()
                .setId(post.getId().toString())
                .setUserId(post.getUserId().toString())
                .setContent(Optional.ofNullable(post.getContent()).orElse(""))
                .setUserName(Optional.ofNullable(post.getUserName()).orElse(""))
                .setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : "");

        if (post.getPostImages() != null && !post.getPostImages().isEmpty()) {
            builder.addAllImageUrls(post.getPostImages());
        }

        if (post.getImage() != null) {
            builder.setProfilePic(post.getImage());
        }

        // Repost fields
        if (post.isRepost() && post.getOriginalPostId() != null) {
            builder.setIsRepost(true).setOriginalPostId(post.getOriginalPostId().toString());
            if (post.getRepostedBy() != null) builder.setRepostedBy(post.getRepostedBy().toString());
            if (post.getRepostedAt() != null) builder.setRepostedAt(post.getRepostedAt().toString());
        }

        return builder.build();
    }

    /**
     * Creates a new post with denormalized user details fetched from Auth Service via gRPC.
     */
    public Post createPost(PostRequest req) {

        log.info("Creating post for userId={}", req.getUserId());

        UUID userId = req.getUserId();
        if (userId == null) {
            log.error("UserId is null during post creation");
            throw new IllegalArgumentException("User ID must not be null");
        }

        UserResponse user;
        try {
            UserRequest request = UserRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();

            user = userStub.getUserById(request);
        } catch (Exception e) {
            log.error("Failed to fetch user info from auth-service for userId={}", userId, e);
            throw new RuntimeException("User service unavailable", e);
        }

        // Create post with user snapshot information
        Post post = new Post();
        post.setContent(req.getContent());
        post.setPostImages(req.getPostImages());
        post.setCreatedAt(LocalDateTime.now());

        // Denormalized user info
        post.setUserId(UUID.fromString(user.getUserId()));
        post.setUserName(user.getUserName());
        post.setDescription(user.getDescription());
        post.setImage(user.getImage());
        post.setDistrict(user.getDistrict());

        Post saved = postRepository.save(post);

        log.info("Post created successfully | postId={}", saved.getId());
        return saved;
    }


    /**
     * Reposts an existing post.
     */
    public Post repost(UUID originalPostId, UUID userId, String userName, String userImage) {
        log.info("Reposting post | originalPostId={}, userId={}", originalPostId, userId);

        Post original = postRepository.findById(originalPostId)
                .orElseThrow(() -> {
                    log.error("Original post not found: {}", originalPostId);
                    return new NoSuchElementException("Original post not found");
                });

        Post repost = new Post();
        repost.setRepost(true);
        repost.setOriginalPostId(original.getId());
        repost.setRepostedBy(userId);
        repost.setRepostedAt(LocalDateTime.now());

        // store snapshot of user who reposted
        repost.setUserId(userId);
        repost.setUserName(userName);
        repost.setImage(userImage);

        Post saved = postRepository.save(repost);

        log.info("Repost created successfully | repostId={} originalPostId={}", saved.getId(), originalPostId);

        return saved;
    }

    /**
     * Delete post.
     */
    public void deletePost(UUID postId, UUID userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Ownership check
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this post");
        }

        postRepository.delete(post);

        log.info("Post {} deleted by user {}", postId, userId);
    }

    /**
     * update post.
     */
    public Post updatePost(UUID postId, UUID userId, UpdatePostRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Ownership check
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to edit this post");
        }

        post.setContent(request.getContent());
        post.setPostImages(request.getPostImages());

        log.info("Post {} updated by user {}", postId, userId);

        return postRepository.save(post);
    }
}
