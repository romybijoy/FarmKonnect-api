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
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class PostServiceImpl extends PostServiceGrpc.PostServiceImplBase {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;
    private final PostLikeService postLikeService;
    private final PostSaveService postSaveService;

    @Override
    public void getPostsByUserIds(UserIdsRequest request, StreamObserver<PostListResponse> responseObserver) {
        List<UUID> userIds = request.getUserIdsList().stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<Post> posts = postRepository.findByUserIdInOrderByCreatedAtDesc(userIds);

        List<PostMessage> grpcPosts = posts.stream()
                .map(this::toGrpcPost)
                .collect(Collectors.toList());

        PostListResponse response = PostListResponse.newBuilder()
                .addAllPosts(grpcPosts)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    private PostMessage toGrpcPost(Post post) {
        PostMessage.Builder builder = PostMessage.newBuilder()
                .setId(post.getId().toString())
                .setUserId(post.getUserId().toString())
                .setContent(Optional.ofNullable(post.getContent()).orElse(""))
                .setUserName(Optional.ofNullable(post.getUserName()).orElse(""))
                .setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : "");

        if (post.getPostImage() != null) {
            builder.setImageUrl(post.getPostImage());
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
//            if (post.getRepostedByName() != null) {
//                builder.setRepostedByName(post.getRepostedByName());
//            }
//            if (post.getRepostedByImage() != null) {
//                builder.setRepostedByImage(post.getRepostedByImage());
//            }
            if (post.getRepostedAt() != null) {
                builder.setRepostedAt(post.getRepostedAt().toString());
            }
        }

        return builder.build();
    }



    @Override
    @Transactional
    public void likePost(LikePostRequest request, StreamObserver<LikePostResponse> responseObserver) {
        try {
            UUID postId = UUID.fromString(request.getPostId());
            UUID userId = UUID.fromString(request.getUserId());

            // Ensure post exists
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            boolean liked;

            if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
                postLikeRepository.deleteByPostIdAndUserId(postId, userId);
                liked = false;
            } else {
                Like like = new Like();
                like.setPost(post);
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


    @Override
    @Transactional
    public void savePost(SavePostRequest request, StreamObserver<SavePostResponse> responseObserver) {
        try {
            UUID postId = UUID.fromString(request.getPostId());
            UUID userId = UUID.fromString(request.getUserId());

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            boolean saved;

            // Check if already saved
            Optional<Save> existing = savedPostRepository.findByUserIdAndPostId(postId, userId);
            if (existing.isPresent()) {
                savedPostRepository.delete(existing.get());
                saved = false;
            } else {
                Save save = new Save();
                save.setPost(post);
                save.setUserId(userId);
                save.setSavedAt(LocalDateTime.now());
                savedPostRepository.save(save);
                saved = true;
            }

            SavePostResponse response = SavePostResponse.newBuilder()
                    .setSaved(saved)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error in savePost: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }


    @Override
    public void getSavedPosts(GetSavedPostsRequest request, StreamObserver<GetSavedPostsResponse> responseObserver) {
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

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error in getSavedPosts: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void isPostLikedByUser(IsPostLikedByUserRequest request,
                                  StreamObserver<IsPostLikedByUserResponse> responseObserver) {
        UUID postId = UUID.fromString(request.getPostId());
        UUID userId = UUID.fromString(request.getUserId());

        boolean liked = postLikeService.isPostLikedByUser(postId, userId);

        IsPostLikedByUserResponse response = IsPostLikedByUserResponse.newBuilder()
                .setLiked(liked)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void isPostSavedByUser(IsPostSavedByUserRequest request,
                                  StreamObserver<IsPostSavedByUserResponse> responseObserver) {
        UUID postId = UUID.fromString(request.getPostId());
        UUID userId = UUID.fromString(request.getUserId());

        boolean saved = postSaveService.isPostSavedByUser(postId, userId);

        IsPostSavedByUserResponse response = IsPostSavedByUserResponse.newBuilder()
                .setSaved(saved)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getLikeCount(GetLikeCountRequest request,
                             StreamObserver<GetLikeCountResponse> responseObserver) {
        UUID postId = UUID.fromString(request.getPostId());
        long likeCount = postLikeService.getLikeCount(postId);

        GetLikeCountResponse response = GetLikeCountResponse.newBuilder()
                .setLikeCount((int) likeCount)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Utility method: convert entity -> gRPC message
    private PostMessage mapToGrpc(Post post) {
        return PostMessage.newBuilder()
                .setId(post.getId().toString())
                .setUserId(post.getUserId().toString())
                .setContent(post.getContent())
                .setImageUrl(post.getPostImage())
                .setCreatedAt(post.getCreatedAt().toString())
                .build();
    }

    @Override
    public void getPostById(PostIdRequest request, StreamObserver<PostMessage> responseObserver) {
        UUID postId = UUID.fromString(request.getPostId());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostMessage grpcPost = toGrpcPost(post);

        responseObserver.onNext(grpcPost);
        responseObserver.onCompleted();
    }


}
