package com.fc.postservice.service;

import com.fc.postservice.dto.CommentDto;
import com.fc.postservice.dto.CommentRequest;
import com.fc.postservice.model.Comment;
import com.fc.postservice.repository.CommentRepository;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service that handles comment creation, replies, retrieval,
 * and recursive mapping of comment threads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcClient;

    /**
     * Add a new comment to a post. Supports both root and nested replies.
     */
        public Comment addComment(UUID postId, CommentRequest request) {

            log.info("Adding comment for postId={} by userId={}", postId, request.getUserId());

            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(request.getUserId());
            comment.setContent(request.getContent());

            // If this is a reply to another comment
            if (request.getParentId() != null) {
                Comment parent = commentRepository.findById(request.getParentId())
                        .orElseThrow(() ->  {
                            log.error("Parent comment not found: {}", request.getParentId());
                            return new NoSuchElementException("Parent comment not found");
                        });
                comment.setParentComment(parent);
            }

            Comment saved = commentRepository.save(comment);

            log.info("Comment saved successfully with id={}", saved.getId());
            return saved;
        }

    /**
     * Add a reply to an existing parent comment.
     */
    public Comment addReply(UUID postId, UUID userId, UUID parentCommentId, String content) {

        log.info("Adding reply for postId={} parentCommentId={} by userId={}",
                postId, parentCommentId, userId);

        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> {
                    log.error("Parent comment not found: {}", parentCommentId);
                    return new NoSuchElementException("Parent comment not found");
                });

        Comment reply = new Comment();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setParentComment(parent);

        Comment saved = commentRepository.save(reply);
        log.info("Reply saved successfully with id={}", saved.getId());
        return saved;
    }

    /**
     * Fetch all top-level comments for a post along with their nested replies.
     */
    public List<CommentDto> getCommentsWithReplies(UUID postId) {

        log.info("Fetching comments with replies for postId={}", postId);

        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        return comments.stream()
                .map(this::mapToDtoWithReplies)
                .toList();
    }

    /**
     * Recursive mapping from Comment → CommentDto including replies.
     */
    private CommentDto mapToDtoWithReplies(Comment comment) {
        UUID userId = comment.getUserId();
        if (userId == null) {
            log.error("UserId is null for commentId={}", comment.getId());
            throw new IllegalArgumentException("User ID must not be null");
        }

        // Fetch user details via gRPC
        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        UserResponse user;
        try {
            user = userGrpcClient.getUserById(request);
        } catch (Exception e) {
            log.error("Failed to fetch user info for userId={} via gRPC", userId, e);
            throw new RuntimeException("Failed to fetch user info", e);
        }

        // Build DTO with recursive replies
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPostId())
                .parentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .userId(UUID.fromString(user.getUserId()))
                .userName(user.getUserName())
                .profileImage(user.getImage())
                .replies(
                        comment.getReplies().stream()
                                .map(this::mapToDtoWithReplies)
                                .toList()
                )
                .build();
    }

    /**
     * Get the number of top-level comments for a post.
     */
    public long getCommentCount(UUID postId) {
        log.debug("Counting top-level comments for postId={}", postId);
        return commentRepository.countTopLevelCommentsByPostId(postId);
    }
}

