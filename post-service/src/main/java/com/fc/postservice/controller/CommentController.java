package com.fc.postservice.controller;

import com.fc.postservice.dto.CommentDto;
import com.fc.postservice.dto.CommentRequest;
import com.fc.postservice.model.Comment;
import com.fc.postservice.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Add comment", description = "Adds a new comment to the specified post.")
    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable UUID postId, @RequestBody CommentRequest request) {

        log.debug("Request to add comment for postId={} by userId={}", postId, request.getUserId());
        try {
            Comment saved = commentService.addComment(postId, request);
            log.info("Comment added successfully for postId={} commentId={}", postId, saved.getId());
            return ResponseEntity.ok(saved);
        }
        catch (Exception e) {
            log.error("Failed to add comment for postId={} userId={} : {}",
                    postId, request.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Adds a reply under an existing parent comment.
     *
     * @param postId    ID of the post
     * @param parentId  ID of the parent comment
     * @param userId    ID of the replying user (query param)
     * @param content   text content of the reply
     * @return saved reply Comment entity
     */
    @Operation(summary = "Add reply", description = "Adds a reply to a specific parent comment.")
    @PostMapping("/{parentId}/reply")
    public Comment addReply(@PathVariable UUID postId,
                            @PathVariable UUID parentId,
                            @RequestParam UUID userId,
                            @RequestBody String content) {

        log.debug("Request to add reply for postId={}, parentId={}, userId={}",
                postId, parentId, userId);

        try {
            Comment reply = commentService.addReply(postId, userId, parentId, content);

            log.info("Reply added: replyId={} for postId={} parentId={}",
                    reply.getId(), postId, parentId);

            return reply;
        } catch (Exception e) {
            log.error("Failed to add reply for postId={} parentId={} userId={} : {}",
                    postId, parentId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all comments for a post, including nested replies.
     *
     * @param postId ID of the post
     * @return List of CommentDto (comments + replies)
     */
    @Operation(summary = "Get comments", description = "Fetches all comments for a post, including replies.")
    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID postId) {
        log.debug("Fetching comments for postId={}", postId);
        List<CommentDto> result = commentService.getCommentsWithReplies(postId);
        log.info("Fetched {} comments for postId={}", result.size(), postId);
        return ResponseEntity.ok(result);
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
}
