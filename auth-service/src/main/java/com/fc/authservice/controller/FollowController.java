package com.fc.authservice.controller;

import com.fc.authservice.dto.FollowCountResponse;
import com.fc.authservice.dto.FollowRequest;
import com.fc.authservice.dto.FollowUserDTO;
import com.fc.authservice.repository.FollowRepository;
import com.fc.authservice.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * REST controller for managing the follow relationships between users.
 * Provides APIs to follow/unfollow users, fetch followers/following lists,
 * get follow counts, and check if one user is following another.
 * This controller delegates core follow logic to {@link FollowService} and,
 * for quick existence checks, uses {@link FollowRepository}.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
@RestController
@Tag(name = "Follow", description = "APIs for managing follower and following relationships")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;


    /**
     * Allows a user to follow another user.
     *
     * @param request contains followerId and followingId
     * @return HTTP 200 with success message on completion
     */
    @PostMapping("/follow")
    @Operation(
            summary = "Follow a user",
            description = "Creates a follow relationship where followerId starts following followingId."
    )
    public ResponseEntity<String> follow(@RequestBody FollowRequest request) {
        log.info("POST /follow - followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        followService.followUser(request.getFollowerId(), request.getFollowingId());
        log.info("POST /follow - completed for followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok("Followed successfully.");
    }

    /**
     * Allows a user to unfollow another user.
     *
     * @param request contains followerId and followingId
     * @return HTTP 200 with success message on completion
     */
    @DeleteMapping("/unfollow")
    @Operation(
            summary = "Unfollow a user",
            description = "Removes the follow relationship between followerId and followingId."
    )
    public ResponseEntity<String> unfollow(@RequestBody FollowRequest request) {
        log.info("DELETE /unfollow - followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        followService.unfollowUser(request.getFollowerId(), request.getFollowingId());
        log.info("DELETE /unfollow - completed for followerId={} followingId={}", request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok("Unfollowed successfully.");
    }

    /**
     * Retrieves the list of users who follow the given user.
     *
     * @param userId the user whose followers are being requested
     * @return list of followers wrapped in FollowUserDTO
     */
    @GetMapping("/followers/{userId}")
    @Operation(
            summary = "Get followers of a user",
            description = "Returns a list of users who are following the specified user."
    )
    public ResponseEntity<List<FollowUserDTO>> getFollowers(@PathVariable UUID userId) {
        log.info("GET /followers/{} - fetching followers", userId);
        List<FollowUserDTO> followers = followService.getFollowers(userId);
        log.info("GET /followers/{} - returning {} followers", userId, followers != null ? followers.size() : 0);
        return ResponseEntity.ok(followers);
    }

    /**
     * Retrieves the list of users that the given user is following.
     *
     * @param userId the user whose following list is being requested
     * @return list of following users wrapped in FollowUserDTO
     */
    @GetMapping("/following/{userId}")
    @Operation(
            summary = "Get users that a user is following",
            description = "Returns a list of users that the specified user is following."
    )
    public ResponseEntity<List<FollowUserDTO>> getFollowing(@PathVariable UUID userId) {
        log.info("GET /following/{} - fetching following", userId);
        List<FollowUserDTO> following = followService.getFollowing(userId);
        log.info("GET /following/{} - returning {} following", userId, following != null ? following.size() : 0);
        return ResponseEntity.ok(following);
    }

    /**
     * Retrieves the count of followers and following for a given user.
     *
     * @param userId the user whose follower and following counts are requested
     * @return an object containing counts for followers and following
     */
    @GetMapping("/follow/count/{userId}")
    @Operation(
            summary = "Get follow counts for a user",
            description = "Returns the number of followers and following for the specified user."
    )
    public ResponseEntity<FollowCountResponse> getFollowCounts(@PathVariable UUID userId) {
        log.info("GET /follow/count/{} - fetching counts", userId);
        long followers = followService.getFollowerCount(userId);
        long following = followService.getFollowingCount(userId);
        log.info("GET /follow/count/{} - followers={} following={}", userId, followers, following);
        return ResponseEntity.ok(new FollowCountResponse(userId, followers, following));
    }

    /**
     * Checks whether a given followerId is following a given followingId.
     *
     * @param followerId  the potential follower user ID
     * @param followingId the target user ID to check against
     * @return a map with a single key 'following' indicating true/false
     */
    @GetMapping("/is-following")
    @Operation(
            summary = "Check if a user is following another user",
            description = "Returns a boolean flag indicating whether followerId is following followingId."
    )
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
