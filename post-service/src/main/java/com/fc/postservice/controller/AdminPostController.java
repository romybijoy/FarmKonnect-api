package com.fc.postservice.controller;

import com.fc.postservice.dto.admin.PostDetailAdminDto;
import com.fc.postservice.dto.admin.PostResponse;
import com.fc.postservice.service.AdminPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    public AdminPostController(AdminPostService adminPostService) {
        this.adminPostService = adminPostService;
    }

    /**
     * GET /admin/posts
     */
    @GetMapping
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder
    ) {
        PostResponse postsResponse = adminPostService.getAllPosts(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(postsResponse, HttpStatus.FOUND);
    }

    /**
     * GET /admin/posts/{id}
     */
    @GetMapping("/{id}")
    public PostDetailAdminDto getPost(@PathVariable("id") UUID id) {
        try {
            return adminPostService.getPostDetail(id);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    /**
     * POST /admin/posts/{id}/actions
     * Placeholder to perform admin actions (remove/restore/pin/warn).
     * Extend this to call moderation logic, audit logging, and event publishing.
     */
    @PostMapping("/{id}/actions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actionOnPost(@PathVariable("id") UUID id, @RequestBody AdminActionRequest req) {
        // For now, just validate post existence and return 204.
        // Replace with actual moderation logic (update post status, write mod log, publish event).
        try {
            adminPostService.getPostDetail(id); // ensures post exists
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        // TODO: implement actions: remove, restore, pin, warn_user, suspend_user, etc.
    }

    // Simple DTO for admin action request (you can move to a separate file)
    public static class AdminActionRequest {
        public String action;      // e.g. "remove", "restore", "pin"
        public String reason;
        public String moderatorId;
    }
}
