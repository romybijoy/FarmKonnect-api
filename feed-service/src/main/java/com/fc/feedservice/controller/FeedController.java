package com.fc.feedservice.controller;

import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.service.FeedServiceImpl;
import com.fc.feedservice.service.HiddenPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedServiceImpl feedService;

    private final HiddenPostService hiddenPostService;

    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @GetMapping("/{userId}")
    public List<PostDto> getFeed(@PathVariable UUID userId) {
        return feedService.getFeed(userId);
    }

    @PostMapping("/{userId}/hide/{postId}")
    public ResponseEntity<Void> hidePostFromFeed(
            @PathVariable UUID userId,
            @PathVariable UUID postId) {
        logger.debug("hidePostFromFeed reached for userId={} postId={}", userId, postId);
        hiddenPostService.hidePost(userId, postId);
        return ResponseEntity.ok().build();
    }
}
