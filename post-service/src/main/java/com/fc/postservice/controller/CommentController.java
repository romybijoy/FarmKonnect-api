package com.fc.postservice.controller;

import com.fc.postservice.dto.CommentDto;
import com.fc.postservice.dto.CommentRequest;
import com.fc.postservice.dto.ReactionRequest;
import com.fc.postservice.dto.UpdateCommentRequest;
import com.fc.postservice.model.Comment;
import com.fc.postservice.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Controller for managing comments and replies on posts.
 * Endpoints include:
 *  - Add a new comment
 *  - Add a reply to an existing comment
 *  - Get all comments (with nested replies)
 *  - Get comment count for a post
 */
@Slf4j
@RestController
@RequestMapping("/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments API", description = "Handles comments and replies for posts")
public class CommentController {
    private final CommentService commentService;

    /**
     * Adds a new comment to a post.
     *
     * @param postId  ID of the post receiving the comment
     * @param request request body containing comment content & userId
     * @return saved Comment entity
     */
    @Operation(summary = "Add comment/reply", description = "Adds a new comment/reply to the specified post.")
    @PostMapping
    public ResponseEntity<CommentDto> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        CommentDto saved =
                commentService.addComment(postId, userId, request);

        return ResponseEntity.ok(saved);

    }
    /**
     * Retrieves all comments for a post, including nested replies.
     *
     * @param postId ID of the post
     * @return List of CommentDto (comments + replies)
     */
    @GetMapping
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {

        UUID userId = UUID.fromString(principal.getName());

        Page<CommentDto> result =
                commentService.getComments(postId, userId, page, size);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentDto>> getReplies(
            @PathVariable UUID commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Principal principal
    ) {

        UUID userId = UUID.fromString(principal.getName());
        log.info("Principal object: {}", principal);
        Page<CommentDto> replies =
                commentService.getReplies(commentId, userId, page, size);

        return ResponseEntity.ok(replies);
    }

    /**
     * Retrieves total number of comments on a post.
     *
     * @param postId ID of the post
     * @return number of comments
     */
    @Operation(summary = "Get comment count", description = "Returns the count of comments for a post.")
    @GetMapping("/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable UUID postId) {
        log.debug("Fetching comment count for postId={}", postId);

        try {
            long count = commentService.getCommentCount(postId);
            log.info("Comment count for postId={} is {}", postId, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to fetch comment count for postId={} : {}", postId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * ❤️ Toggle like
     */
    @Operation(summary = "Toggle like on a comment")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable UUID commentId,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.info("User {} toggling like on comment {}", userId, commentId);

        commentService.toggleLike(commentId, userId);

        log.debug("Like toggled successfully for comment {}", commentId);

        return ResponseEntity.ok().build();
    }

    /**
     * 😀 Toggle reaction
     */
    @Operation(summary = "Toggle emoji reaction on a comment")
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<Void> toggleReaction(
            @PathVariable UUID commentId,
            @Valid @RequestBody ReactionRequest request,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.info("User {} reacting with '{}' on comment {}",
                userId, request.getEmoji(), commentId);

        commentService.toggleReaction(commentId, userId, request.getEmoji());

        log.debug("Reaction updated successfully for comment {}", commentId);

        return ResponseEntity.ok().build();
    }

    /**
     * ✏ Edit comment
     */
    @Operation(summary = "Update a comment")
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable UUID commentId,
            @RequestBody UpdateCommentRequest request,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.info("User {} updating comment {}", userId, commentId);

        commentService.updateComment(commentId, userId, request.getContent());

        log.debug("Comment {} updated successfully", commentId);

        return ResponseEntity.ok().build();
    }

    /**
     * 🗑 Soft delete
     */
    @Operation(summary = "Soft delete a comment")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.warn("User {} deleting comment {}", userId, commentId);

        commentService.deleteComment(commentId, userId);

        log.debug("Comment {} soft deleted", commentId);

        return ResponseEntity.ok().build();
    }
}
