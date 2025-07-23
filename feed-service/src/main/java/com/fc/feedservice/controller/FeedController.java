package com.fc.feedservice.controller;

import com.fc.feedservice.client.FollowServiceClient;
import com.fc.feedservice.client.PostServiceClient;
import com.fc.feedservice.dto.LikeResponseDto;
import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.service.FeedServiceImpl;
import com.postservice.PostMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedServiceImpl feedService;

    @GetMapping("/{userId}")
    public List<PostDto> getFeed(@PathVariable UUID userId) {
        return feedService.getFeed(userId);
    }

    @PostMapping("/{userId}/like/{postId}")
    public ResponseEntity<LikeResponseDto> likePost(@PathVariable UUID userId, @PathVariable UUID postId) {
        return ResponseEntity.ok(feedService.likePost(userId, postId));
    }


}
