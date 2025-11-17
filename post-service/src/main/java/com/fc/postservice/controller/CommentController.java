package com.fc.postservice.controller;

import com.fc.postservice.dto.CommentDto;
import com.fc.postservice.dto.CommentRequest;
import com.fc.postservice.model.Comment;
import com.fc.postservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable UUID postId, @RequestBody CommentRequest request) {

        Comment saved = commentService.addComment(postId, request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{parentId}/reply")
    public Comment addReply(@PathVariable UUID postId,
                            @PathVariable UUID parentId,
                            @RequestParam UUID userId,
                            @RequestBody String content) {
        return commentService.addReply(postId, userId, parentId, content);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID postId) {
        List<CommentDto> result = commentService.getCommentsWithReplies(postId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable UUID postId) {
        long count = commentService.getCommentCount(postId);
        return ResponseEntity.ok(count);
    }
}
