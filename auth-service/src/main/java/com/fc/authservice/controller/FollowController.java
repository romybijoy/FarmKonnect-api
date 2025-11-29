package com.fc.authservice.controller;

import com.fc.authservice.dto.FollowCountResponse;
import com.fc.authservice.dto.FollowRequest;
import com.fc.authservice.dto.FollowUserDTO;
import com.fc.authservice.repository.FollowRepository;
import com.fc.authservice.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class FollowController {

    private static final Logger log = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @PostMapping("/follow")
    public ResponseEntity<String> follow(@RequestBody FollowRequest request) {
        log.info("POST /follow - followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        followService.followUser(request.getFollowerId(), request.getFollowingId());
        log.info("POST /follow - completed for followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok("Followed successfully.");
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<String> unfollow(@RequestBody FollowRequest request) {
        log.info("DELETE /unfollow - followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        followService.unfollowUser(request.getFollowerId(), request.getFollowingId());
        log.info("DELETE /unfollow - completed for followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok("Unfollowed successfully.");
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<FollowUserDTO>> getFollowers(@PathVariable UUID userId) {
        log.info("GET /followers/{} - fetching followers", userId);
        List<FollowUserDTO> followers = followService.getFollowers(userId);
        log.info("GET /followers/{} - returning {} followers", userId, followers != null ? followers.size() : 0);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<FollowUserDTO>> getFollowing(@PathVariable UUID userId) {
        log.info("GET /following/{} - fetching following", userId);
        List<FollowUserDTO> following = followService.getFollowing(userId);
        log.info("GET /following/{} - returning {} following", userId, following != null ? following.size() : 0);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/follow/count/{userId}")
    public ResponseEntity<FollowCountResponse> getFollowCounts(@PathVariable UUID userId) {
        log.info("GET /follow/count/{} - fetching counts", userId);
        long followers = followService.getFollowerCount(userId);
        long following = followService.getFollowingCount(userId);
        log.info("GET /follow/count/{} - followers={} following={}", userId, followers, following);
        return ResponseEntity.ok(new FollowCountResponse(userId, followers, following));
    }

    @GetMapping("/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @RequestParam UUID followerId,
            @RequestParam UUID followingId
    ) {
        log.info("GET /is-following - followerId={} followingId={}", followerId, followingId);
        boolean status = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        log.info("GET /is-following - result followerId={} followingId={} following={}", followerId, followingId, status);
        return ResponseEntity.ok(Collections.singletonMap("following", status));
    }


}
