package com.fc.postservice.controller;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.dto.PostRequest;
import com.fc.postservice.dto.UpdatePostRequest;
import com.fc.postservice.dto.UserDto;
import com.fc.postservice.model.Post;
import com.fc.postservice.service.PostLikeService;
import com.fc.postservice.service.PostSaveService;
import com.fc.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fc.postservice.mapper.PostMapper.mapToDto;

/**
 * REST controller for managing posts, including:
 * - Creating posts
 * - Liking / unliking
 * - Saving / unsaving
 * - Reposting
 * - Retrieving posts
 */
@Slf4j
@RestController
@Tag(name = "Posts", description = "API for managing posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PostLikeService postLikeService;
    @Autowired
    private PostSaveService postSaveService;

    /**
     * Get all posts.
     */
    @GetMapping("/get")
    @Operation(summary = "Get all posts", description = "Fetches all posts in the system.")
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        log.debug("GET /posts/get - Fetching all posts");

        try {
        List<PostDTO> posts = postService.getAllPosts();
        log.info("Fetched {} posts", posts.size());
        return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Error fetching posts: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetch all posts created by a specific user.
     *
     * @param userId ID of the user
     * @param page   Page number (default 0)
     * @param size   Page size (default 10)
     * @return Paginated posts created by the user
     */
    @GetMapping("/user/{userId}")
    public Page<Post> getPostsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        log.info("Received request to fetch posts for userId={}", userId);

        Page<Post> response = postService.getPostsByUserId(userId, page, size);

        log.info("Returning {} posts for userId={}",
                response.getNumberOfElements(), userId);

        return response;
    }

    /**
     * Create a new post.
     */
    @PostMapping("/create")
    @Operation(summary = "Create new post", description = "Creates a new post for a user.")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        log.debug("POST /posts/create - Creating post for userId={}", request.getUserId());

        try {
        Post created = postService.createPost(request);
        log.info("Post created with id={}", created.getId());
        return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create post: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ---------- LIKE ----------

    /**
     * Like a post.
     */
    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<String> likePost(@PathVariable UUID postId,
                                           @Parameter(description = "User ID who likes the post") @RequestParam UUID userId) {
        log.debug("Liking postId={} by userId={}", postId, userId);

        try {
            postLikeService.likePost(postId, userId);

            log.info("User {} liked post {}", userId, postId);

            return ResponseEntity.ok("Post liked");
        } catch (Exception e) {
            log.error("Failed to like postId={} by userId={}: {}", postId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Unlike a post.
     */
    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<String> unlikePost(@PathVariable UUID postId,
                                             @RequestParam UUID userId) {
        log.debug("Unliking postId={} by userId={}", postId, userId);

        try {
        postLikeService.unlikePost(postId, userId);
        log.info("User {} unliked post {}", userId, postId);
        return ResponseEntity.ok("Post unliked");
        } catch (Exception e) {
            log.error("Failed to unlike postId={} by userId={}: {}", postId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Check if user liked a post.
     */
    @GetMapping("/{postId}/like-status")
    @Operation(summary = "Check like status")
    public Map<String, Boolean> isPostLiked(@PathVariable UUID postId,
                               @RequestParam UUID userId) {

        log.debug("Checking like status postId={} userId={}", postId, userId);

        boolean liked = postLikeService.isPostLikedByUser(postId, userId);
        log.info("Like status for postId={} userId={} = {}", postId, userId, liked);

        return Map.of("liked", liked);
    }

    /**
     * Get like count.
     */
    @GetMapping("/{postId}/like-count")
    @Operation(summary = "Get like count")
    public Map<String, Long> getLikeCount(@PathVariable UUID postId) {
        log.debug("Fetching like count for postId={}", postId);

        long count = postLikeService.getLikeCount(postId);
        log.info("Like count for postId={} = {}", postId, count);
        return Map.of("count", count);
    }

    // ---------- SAVE ----------

    /**
     * Save a post.
     */
    @PostMapping("/{postId}/save")
    @Operation(summary = "Save post")
    public ResponseEntity<String> savePost(@PathVariable UUID postId,
                                           @RequestParam UUID userId) {
        log.debug("Saving postId={} for userId={}", postId, userId);

        try {
        postSaveService.savePost(postId, userId);
        log.info("Post saved: postId={} userId={}", postId, userId);
        return ResponseEntity.ok("Post saved");
        } catch (Exception e) {
            log.error("Failed to save postId={} for userId={}: {}", postId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{postId}/save")
    @Operation(summary = "Unsave post")
    public ResponseEntity<String> unsavePost(@PathVariable UUID postId,
                                             @RequestParam UUID userId) {
        log.debug("Unsaving postId={} for userId={}", postId, userId);

        try {
        postSaveService.unsavePost(postId, userId);
        log.info("Post unsaved: postId={} userId={}", postId, userId);

        return ResponseEntity.ok("Post unsaved");
        } catch (Exception e) {
            log.error("Failed to unsave postId={} for userId={}: {}", postId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{postId}/save-status")
    @Operation(summary = "Check save status")
    public boolean isPostSaved(@PathVariable UUID postId,
                               @RequestParam UUID userId) {
        log.debug("Checking save status for postId={} userId={}", postId, userId);
        boolean saved = postSaveService.isPostSavedByUser(postId, userId);
        log.info("Save status for postId={} userId={} = {}", postId, userId, saved);
        return saved;
    }

    @GetMapping("/{postId}/save-count")
    @Operation(summary = "Get save count")
    public long getSaveCount(@PathVariable UUID postId) {
        log.debug("Fetching save count for postId={}", postId);
        long count = postSaveService.getSaveCount(postId);
        log.info("Save count for postId={} = {}", postId, count);
        return count;
    }

    @GetMapping("/saved/{userId}")
    @Operation(summary = "Get saved posts")
    public ResponseEntity<List<PostDTO>> getSavedPosts(@PathVariable UUID userId) {

        log.debug("Fetching saved posts for userId={}", userId);

        try {
            List<PostDTO> posts = postSaveService.getSavedPosts(userId);
            log.info("Fetched {} saved posts for userId={}", posts.size(), userId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Failed to fetch saved posts for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Repost an existing post.
     */
    @PostMapping("/{postId}/repost/{userId}")
    @Operation(summary = "Repost a post", description = "User reposts an existing post.")
    public ResponseEntity<PostDTO> repost(
            @PathVariable UUID postId,
            @PathVariable UUID userId,
            @RequestBody UserDto userDto) {

        log.debug("Reposting postId={} by userId={}", postId, userId);

        try {
            Post repost = postService.repost(postId, userId, userDto.getName(), userDto.getProfileImage());
            log.info("Repost created: postId={} by userId={}", repost.getId(), userId);
            return ResponseEntity.ok(mapToDto(repost));
        } catch (Exception e) {
            log.error("Failed to repost postId={} by userId={}: {}", postId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            @RequestParam UUID userId
    ) {
        log.info("Delete request for postId={} by userId={}", postId, userId);

        postService.deletePost(postId, userId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @PathVariable UUID postId,
            @RequestParam UUID userId,
            @RequestBody UpdatePostRequest request
    ) {
        log.info("Update request for postId={} by userId={}", postId, userId);

        Post updated = postService.updatePost(postId, userId, request);

        return ResponseEntity.ok(updated);
    }
}


