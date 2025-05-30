package com.fc.postservice.controller;

import com.fc.postservice.dto.PostRequest;
import com.fc.postservice.model.Post;
import com.fc.postservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/get")
    public String getProductsReviewHandler(){

        return "Hello everyone";
    }

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post created = postService.createPost(request.getEmail(), request.getContent());
        return ResponseEntity.ok(created);
    }
    }

