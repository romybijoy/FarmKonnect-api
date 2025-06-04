package com.fc.postservice.controller;

import com.fc.postservice.dto.PostRequest;
import com.fc.postservice.model.Post;
import com.fc.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Posts", description = "API for managing posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/get")
    public String getProductsReviewHandler(){

        return "Hello everyone";
    }

    @PostMapping("/create")
    @Operation(summary = "Create post")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post created = postService.createPost(request.getEmail(), request.getContent());
        return ResponseEntity.ok(created);
    }
    }

