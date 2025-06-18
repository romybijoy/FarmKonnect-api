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

@RestController
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @PostMapping("/follow")
    public ResponseEntity<String> follow(@RequestBody FollowRequest request) {
        followService.followUser(request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok("Followed successfully.");
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<String> unfollow(@RequestBody FollowRequest request) {
        followService.unfollowUser(request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok("Unfollowed successfully.");
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<FollowUserDTO>> getFollowers(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<FollowUserDTO>> getFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/follow/count/{userId}")
    public ResponseEntity<FollowCountResponse> getFollowCounts(@PathVariable UUID userId) {
        long followers = followService.getFollowerCount(userId);
        long following = followService.getFollowingCount(userId);
        return ResponseEntity.ok(new FollowCountResponse(userId, followers, following));
    }

    @GetMapping("/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @RequestParam UUID followerId,
            @RequestParam UUID followingId
    ) {
        boolean status = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        return ResponseEntity.ok(Collections.singletonMap("following", status));
    }


}

