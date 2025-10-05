package com.fc.postservice.controller;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.dto.PostRequest;
import com.fc.postservice.dto.UserDto;
import com.fc.postservice.mapper.PostMapper;
import com.fc.postservice.model.Post;
import com.fc.postservice.service.PostLikeService;
import com.fc.postservice.service.PostSaveService;
import com.fc.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fc.postservice.mapper.PostMapper.mapToDto;

@RestController
@Tag(name = "Posts", description = "API for managing posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PostLikeService postLikeService;
    @Autowired
    private PostSaveService postSaveService;

    private PostMapper postMapper;

    @GetMapping("/get")
    @Operation(summary = "Get post")
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/create")
    @Operation(summary = "Create post")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post created = postService.createPost(request);
        return ResponseEntity.ok(created);
    }

    // ---------- LIKE ----------
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable UUID postId,
                                           @RequestParam UUID userId) {
        postLikeService.likePost(postId, userId);
        return ResponseEntity.ok("Post liked");
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<String> unlikePost(@PathVariable UUID postId,
                                             @RequestParam UUID userId) {
        postLikeService.unlikePost(postId, userId);
        return ResponseEntity.ok("Post unliked");
    }

    @GetMapping("/{postId}/like-status")
    public Map<String, Boolean> isPostLiked(@PathVariable UUID postId,
                               @RequestParam UUID userId) {
        boolean liked = postLikeService.isPostLikedByUser(postId, userId);
        return Map.of("liked", liked);
    }

    @GetMapping("/{postId}/like-count")
    public Map<String, Long> getLikeCount(@PathVariable UUID postId) {
        long count = postLikeService.getLikeCount(postId);
        return Map.of("count", count);
    }

    // ---------- SAVE ----------
    @PostMapping("/{postId}/save")
    public ResponseEntity<String> savePost(@PathVariable UUID postId,
                                           @RequestParam UUID userId) {
        postSaveService.savePost(postId, userId);
        return ResponseEntity.ok("Post saved");
    }

    @DeleteMapping("/{postId}/save")
    public ResponseEntity<String> unsavePost(@PathVariable UUID postId,
                                             @RequestParam UUID userId) {
        postSaveService.unsavePost(postId, userId);
        return ResponseEntity.ok("Post unsaved");
    }

    @GetMapping("/{postId}/save-status")
    public boolean isPostSaved(@PathVariable UUID postId,
                               @RequestParam UUID userId) {
        return postSaveService.isPostSavedByUser(postId, userId);
    }

    @GetMapping("/{postId}/save-count")
    public long getSaveCount(@PathVariable UUID postId) {
        return postSaveService.getSaveCount(postId);
    }

    @GetMapping("/saved/{userId}")
    public ResponseEntity<List<PostDTO>> getSavedPosts(@PathVariable UUID userId) {
        return ResponseEntity.ok(postSaveService.getSavedPosts(userId));
    }

    @PostMapping("/{postId}/repost/{userId}")
    public ResponseEntity<PostDTO> repost(
            @PathVariable UUID postId,
            @PathVariable UUID userId,
            @RequestBody UserDto userDto) {

        Post repost = postService.repost(postId, userId, userDto.getName(), userDto.getProfileImage());
        return ResponseEntity.ok(mapToDto(repost));
    }
}


