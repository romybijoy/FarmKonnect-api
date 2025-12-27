package com.fc.feedservice.controller;

import com.fc.feedservice.dto.PostDto;
import com.fc.feedservice.service.FeedServiceImpl;
import com.fc.feedservice.service.HiddenPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing the feed of a user.
 * Provides endpoints:
 * - Get a user's feed
 * - Hide a post from a user's feed
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Feed API", description = "Endpoints related to user feed management")
public class FeedController {

    private final FeedServiceImpl feedService;

    private final HiddenPostService hiddenPostService;

    /**
     * Retrieves the personalized feed for a user.
     *
     * @param userId ID of the user
     * @return list of PostDto objects representing the feed content
     */
    @Operation(
            summary = "Get feed for user",
            description = "Returns the personalized feed generated based on followed users."
    )
    @GetMapping("/{userId}")
    public List<PostDto> getFeed(@PathVariable UUID userId) {
        log.debug("Fetching feed for userId={}", userId);

        return feedService.getFeed(userId);
    }

    /**
     * Allows a user to hide a post from their feed.
     *
     * @param userId ID of the user hiding the post
     * @param postId ID of the post to hide
     * @return 200 OK when successfully hidden
     */
    @Operation(
            summary = "Hide a post from feed",
            description = "Marks a post as hidden for the user so it will no longer appear in their feed."
    )
    @PostMapping("/{userId}/hide/{postId}")
    public ResponseEntity<Void> hidePostFromFeed(
            @PathVariable UUID userId,
            @PathVariable UUID postId) {

        log.debug("hidePostFromFeed reached for userId={} postId={}", userId, postId);

        hiddenPostService.hidePost(userId, postId);
        return ResponseEntity.ok().build();
    }
}
