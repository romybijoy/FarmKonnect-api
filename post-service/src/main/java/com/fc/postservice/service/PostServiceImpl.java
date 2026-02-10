package com.fc.postservice.service;

import com.fc.postservice.model.*;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostLikeRepository;
import com.fc.postservice.repository.SavedPostRepository;
import com.postservice.*;
import com.fc.postservice.repository.PostRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * gRPC Post Service Implementation. Handles:
 * - Fetch posts for feed
 * - Like/unlike toggle
 * - Save/unsave toggle
 * - Fetch saved posts
 * - Fetch single post
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl extends PostServiceGrpc.PostServiceImplBase {

    @Value("${server.port}")
    private String port;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;
    private final PostLikeService postLikeService;
    private final PostSaveService postSaveService;
    private final PostService postService;

    private static final String POST_NOT_FOUND_MSG = "Post not found";

    // ==========================================================================
    // 1️. GET POSTS BY USER IDS
    // ==========================================================================
    @Override
    public void getPostsByUserIds(UserIdsRequest request, StreamObserver<PostListResponse> responseObserver) {
        List<UUID> userIds = request.getUserIdsList().stream()
                .map(UUID::fromString)
                .toList();

        log.info("getPostsByUserIds - userIds: {} - handling on port {}", userIds, port);

        try {
        List<PostMessage> grpcPosts = postService.getPostsByUserIdsAsGrpc(userIds);

        log.info("getPostsByUserIds - returning {} posts for userIds {}", grpcPosts.size(), userIds);

        PostListResponse response = PostListResponse.newBuilder()
                .addAllPosts(grpcPosts)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getPostsByUserIds", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Failed to fetch posts").asRuntimeException()
            );
        }
    }

    // ==========================================================================
    // 2. LIKE / UNLIKE POST
    // ==========================================================================

    @Override
    @Transactional
    public void likePost(LikePostRequest request, StreamObserver<LikePostResponse> responseObserver) {
        log.info("likePost - request: postId={}, userId={}", request.getPostId(), request.getUserId());
        try {
            UUID postId = UUID.fromString(request.getPostId());
            UUID userId = UUID.fromString(request.getUserId());

            // Ensure post exists
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException(POST_NOT_FOUND_MSG));

            boolean liked;

            if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
                postLikeRepository.deleteByPostIdAndUserId(postId, userId);
                liked = false;
                log.info("likePost - removed like for postId={} by userId={}", postId, userId);
            } else {
                Like like = new Like();
                like.setPost(post);
                like.setUserId(userId);
                postLikeRepository.save(like);
                liked = true;
                log.info("likePost - added like for postId={} by userId={}", postId, userId);
            }

            long likeCount = postLikeRepository.countByPostId(postId);

            LikePostResponse response = LikePostResponse.newBuilder()
                    .setLiked(liked)
                    .setLikeCount((int) likeCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("likePost - completed for postId={} userId={} liked={} likeCount={}", postId, userId, liked, likeCount);

        } catch (Exception e) {
            log.error("likePost - error processing request: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error in likePost: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    // ==========================================================================
    // 3. SAVE / UNSAVE POST
    // ==========================================================================
    @Override
    @Transactional
    public void savePost(SavePostRequest request, StreamObserver<SavePostResponse> responseObserver) {
        log.info("savePost - request: postId={}, userId={}", request.getPostId(), request.getUserId());
        try {
            UUID postId = UUID.fromString(request.getPostId());
            UUID userId = UUID.fromString(request.getUserId());

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException(POST_NOT_FOUND_MSG));

            boolean saved;

            // Check if already saved
            Optional<Save> existing = savedPostRepository.findByUserIdAndPostId(postId, userId);
            if (existing.isPresent()) {
                savedPostRepository.delete(existing.get());
                saved = false;
                log.info("savePost - removed save for postId={} userId={}", postId, userId);
            } else {
                Save save = new Save();
                save.setPost(post);
                save.setUserId(userId);
                save.setSavedAt(LocalDateTime.now());
                savedPostRepository.save(save);
                saved = true;
                log.info("savePost - added save for postId={} userId={}", postId, userId);
            }

            SavePostResponse response = SavePostResponse.newBuilder()
                    .setSaved(saved)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("savePost - completed for postId={} userId={} saved={}", postId, userId, saved);

        } catch (Exception e) {
            log.error("savePost - error processing request: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error in savePost: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    // ==========================================================================
    // 4️ . GET SAVED POSTS
    // ==========================================================================

    @Override
    public void getSavedPosts(GetSavedPostsRequest request, StreamObserver<GetSavedPostsResponse> responseObserver) {
        log.info("getSavedPosts - request: userId={}", request.getUserId());
        try {
            UUID userId = UUID.fromString(request.getUserId());

            List<Save> savedPosts = savedPostRepository.findByUserId(userId);

            List<Post> posts = savedPosts.stream()
                    .map(Save::getPost)
                    .toList();

            List<PostMessage> grpcPosts = posts.stream()
                    .map(this::mapToGrpc)
                    .toList();

            GetSavedPostsResponse response = GetSavedPostsResponse.newBuilder()
                    .addAllPosts(grpcPosts)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("getSavedPosts - returning {} saved posts for userId={}", grpcPosts.size(), userId);

        } catch (Exception e) {
            log.error("getSavedPosts - error processing request: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error in getSavedPosts: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    // ==========================================================================
    // 5️. CHECK LIKE
    // ==========================================================================
    @Override
    public void isPostLikedByUser(IsPostLikedByUserRequest request,
                                  StreamObserver<IsPostLikedByUserResponse> responseObserver) {
        log.info("isPostLikedByUser - request: postId={}, userId={}", request.getPostId(), request.getUserId());
        UUID postId = UUID.fromString(request.getPostId());
        UUID userId = UUID.fromString(request.getUserId());

        boolean liked = postLikeService.isPostLikedByUser(postId, userId);

        log.info("isPostLikedByUser - result: postId={}, userId={}, liked={}", postId, userId, liked);

        IsPostLikedByUserResponse response = IsPostLikedByUserResponse.newBuilder()
                .setLiked(liked)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ==========================================================================
    // 6️. CHECK SAVE
    // ==========================================================================
    @Override
    public void isPostSavedByUser(IsPostSavedByUserRequest request,
                                  StreamObserver<IsPostSavedByUserResponse> responseObserver) {
        log.info("isPostSavedByUser - request: postId={}, userId={}", request.getPostId(), request.getUserId());
        UUID postId = UUID.fromString(request.getPostId());
        UUID userId = UUID.fromString(request.getUserId());

        boolean saved = postSaveService.isPostSavedByUser(postId, userId);

        log.info("isPostSavedByUser - result: postId={}, userId={}, saved={}", postId, userId, saved);

        IsPostSavedByUserResponse response = IsPostSavedByUserResponse.newBuilder()
                .setSaved(saved)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ==========================================================================
    // 7️. GET LIKE COUNT
    // ==========================================================================
    @Override
    public void getLikeCount(GetLikeCountRequest request,
                             StreamObserver<GetLikeCountResponse> responseObserver) {
        log.info("getLikeCount - request: postId={}", request.getPostId());
        UUID postId = UUID.fromString(request.getPostId());
        long likeCount = postLikeService.getLikeCount(postId);

        log.info("getLikeCount - result: postId={}, likeCount={}", postId, likeCount);

        GetLikeCountResponse response = GetLikeCountResponse.newBuilder()
                .setLikeCount((int) likeCount)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Utility method: convert entity -> gRPC message
    private PostMessage mapToGrpc(Post post) {
        log.debug("mapToGrpc - mapping post id: {}", post != null ? post.getId() : null);
        PostMessage.Builder builder = PostMessage.newBuilder()
                .setId(post != null ? post.getId().toString() : null)
                .setUserId(post.getUserId().toString())
                .setContent(post.getContent())
                .setCreatedAt(post.getCreatedAt().toString());

        // Handle repeated field safely
        if (post.getPostImages() != null && !post.getPostImages().isEmpty()) {
            builder.addAllImageUrls(post.getPostImages());
        }


        PostMessage result = builder.build();
        log.debug("mapToGrpc - mapped post id: {} -> grpc id: {}", post.getId(), result.getId());
        return result;
    }

    // ==========================================================================
    // 8️. GET POST BY ID
    // ==========================================================================
    @Override
    public void getPostById(PostIdRequest request, StreamObserver<PostMessage> responseObserver) {
        log.info("getPostById - request: postId={}", request.getPostId());
        UUID postId = UUID.fromString(request.getPostId());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("getPostById - post not found: {}", postId);
                    return new RuntimeException(POST_NOT_FOUND_MSG);
                });

        PostMessage grpcPost = toGrpcPost(post);

        responseObserver.onNext(grpcPost);
        responseObserver.onCompleted();
        log.info("getPostById - returning postId={}", postId);
    }

    // ==========================================================================
    //  Utility: Convert Entity → gRPC PostMessage (null-safe)
    // ==========================================================================

    private PostMessage toGrpcPost(Post post) {
        log.debug("toGrpcPost - mapping post id: {}", post != null ? post.getId() : null);

        PostMessage.Builder builder = PostMessage.newBuilder()
                .setId(post != null ? post.getId().toString() : null)
                .setUserId(post.getUserId().toString())
                .setContent(Optional.ofNullable(post.getContent()).orElse(""))
                .setUserName(Optional.ofNullable(post.getUserName()).orElse(""))
                .setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : "");

        if (post.getPostImages() != null && !post.getPostImages().isEmpty()) {
            builder.addAllImageUrls(post.getPostImages());
        }

        if (post.getImage() != null) { // profilePic
            builder.setProfilePic(post.getImage());
        }

        // handle repost details
        if (post.isRepost() && post.getOriginalPostId() != null) {
            builder.setIsRepost(true)
                    .setOriginalPostId(post.getOriginalPostId().toString());

            if (post.getRepostedBy() != null) {
                builder.setRepostedBy(post.getRepostedBy().toString());
            }
            if (post.getRepostedAt() != null) {
                builder.setRepostedAt(post.getRepostedAt().toString());
            }
        }

        // STATUS MAPPING  (IMPORTANT!)
        if (post.getStatus() != null) {
            builder.setStatus(PostStatus.valueOf(post.getStatus().name()));
        } else {
            builder.setStatus(PostStatus.POST_STATUS_UNSPECIFIED);
        }
        PostMessage result = builder.build();
        log.debug("toGrpcPost - mapped post id: {} -> grpc id: {}", post.getId(), result.getId());
        return result;
    }

}
