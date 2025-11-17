package com.fc.postservice.service;

import com.fc.postservice.dto.CommentDto;
import com.fc.postservice.dto.CommentRequest;
import com.fc.postservice.model.Comment;
import com.fc.postservice.repository.CommentRepository;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcClient;

        public Comment addComment(UUID postId, CommentRequest request) {
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(request.getUserId());
            comment.setContent(request.getContent());
            if (request.getParentId() != null) {
                Comment parent = commentRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent comment not found"));
                comment.setParentComment(parent);
            }
            return commentRepository.save(comment);
        }

    public Comment addReply(UUID postId, UUID userId, UUID parentCommentId, String content) {
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setParentComment(parent);

        return commentRepository.save(reply);
    }

    public List<CommentDto> getCommentsWithReplies(UUID postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        return comments.stream()
                .map(this::mapToDtoWithReplies)
                .toList();
    }

    private CommentDto mapToDtoWithReplies(Comment comment) {
        UUID userId = comment.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
        UserResponse user = userGrpcClient.getUserById(request);

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
                                .map(this::mapToDtoWithReplies) // recursive mapping
                                .toList()
                )
                .build();
    }

    public long getCommentCount(UUID postId) {
        return commentRepository.countTopLevelCommentsByPostId(postId);
    }
}

