package com.fc.feedservice.controller;

import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.service.FeedServiceImpl;
import com.fc.feedservice.service.HiddenPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedServiceImpl feedService;

    private final HiddenPostService hiddenPostService;

   @GetMapping("/{userId}")
    public List<PostDto> getFeed(@PathVariable UUID userId) {
        return feedService.getFeed(userId);
    }

    @PostMapping("/repost/{postId}")
    public ResponseEntity<?> repost(
            @PathVariable UUID postId,
            @RequestHeader("userId") UUID userId) {

        UUID newPostId = feedService.repost(userId, postId);
        return ResponseEntity.ok(Map.of("repostedPostId", newPostId));
    }


    @PostMapping("/{userId}/hide/{postId}")
    public ResponseEntity<Void> hidePostFromFeed(
            @PathVariable UUID userId,
            @PathVariable UUID postId) {
       System.out.println("reached");
        hiddenPostService.hidePost(userId, postId);
        return ResponseEntity.ok().build();
    }
    }


