package com.fc.authservice.service;

import com.fc.authservice.dto.FollowUserDTO;
import com.fc.authservice.kafka.KafkaProducer;
import com.fc.authservice.model.FollowRelationship;
import com.fc.authservice.model.User;
import com.fc.authservice.repository.FollowRepository;
import com.fc.authservice.repository.UserRepository;
import com.fc.notification.FollowEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing follow and unfollow actions between users.
 * Responsibilities:
 * - Creating and removing follow relationships
 * - Fetching follower & following lists
 * - Counting followers/following
 * - Publishing follow events to Kafka for notification service
 * This service logs all important actions for monitoring and debugging.
 * Author: Romy Rose Jimmy
 * Since: 2025
 */
@Slf4j
@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaProducer kafkaProducer;

    /**
     * Allows a user to follow another user. Creates a relationship and publishes
     * a Kafka event so that the Notification microservice can handle the update.
     *
     * @param followerId  the user who is performing the follow action
     * @param followingId the user being followed
     */
    public void followUser(UUID followerId, UUID followingId) {

        log.info("Follow request: followerId={}, followingId={}", followerId, followingId);

        if (followerId.equals(followingId)) {
            log.warn("User attempted to follow themselves. followerId={}", followerId);
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        boolean alreadyFollowing =
                followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);

        if (alreadyFollowing) {
            log.info("Follow relationship already exists. followerId={}, followingId={}",
                    followerId, followingId);
            return;
        }

        // Create relationship
        FollowRelationship relationship = new FollowRelationship();
        relationship.setFollowerId(followerId);
        relationship.setFollowingId(followingId);
        relationship.setFollowedAt(LocalDateTime.now());
        followRepository.save(relationship);

        log.info("Follow relationship created successfully. followerId={}, followingId={}",
                followerId, followingId);

        // Fetch follower details
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> {
                    log.error("Follower not found. followerId={}", followerId);
                    return new RuntimeException("Follower not found");
                });

        // Build Kafka event
        FollowEvent event = FollowEvent.newBuilder()
                .setSenderId(followerId.toString())
                .setRecipientId(followingId.toString())
                .setSenderName(follower.getUserName())
                .setSenderProfilePic(follower.getImage())
                .setTimestamp(relationship.getFollowedAt()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli())
                .build();

        // Publish event to Kafka
        kafkaProducer.sendFollowEvent(event);

        log.info("Follow event published to Kafka for followerId={} -> followingId={}",
                followerId, followingId);
    }

    /**
     * Removes a follow relationship between two users.
     *
     * @param followerId  the user unfollowing
     * @param followingId the user being unfollowed
     */
    @Transactional
    public void unfollowUser(UUID followerId, UUID followingId) {
        log.info("Unfollow request: followerId={}, followingId={}", followerId, followingId);

        FollowRelationship relationship =
                (FollowRelationship) followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                        .orElseThrow(() -> {
                            log.error("Relationship not found for unfollow. followerId={}, followingId={}",
                                    followerId, followingId);
                            return new RuntimeException("Relationship not found");
                        });

        followRepository.delete(relationship);

        log.info("Unfollow successful. followerId={}, followingId={}", followerId, followingId);
    }

    /**
     * Fetches all followers of a user.
     */
    public List<FollowUserDTO> getFollowers(UUID userId) {
        log.debug("Fetching followers for userId={}", userId);

        List<FollowRelationship> relationships = followRepository.findByFollowingId(userId);

        return relationships.stream()
                .map(rel -> userRepository.findById(rel.getFollowerId()))
                .filter(Optional::isPresent)
                .map(optUser -> {
                    User user = optUser.get();
                    return new FollowUserDTO(
                            user.getId(),
                            user.getUserName(),
                            user.getImage(),
                            user.getEmail()
                    );
                })
                .toList();
    }

    /**
     * Fetches all users that the given user is following.
     */
    public List<FollowUserDTO> getFollowing(UUID userId) {
        log.debug("Fetching following list for userId={}", userId);

        List<FollowRelationship> relationships = followRepository.findByFollowerId(userId);

        return relationships.stream()
                .map(rel -> userRepository.findById(rel.getFollowingId()))
                .filter(Optional::isPresent)
                .map(optUser -> {
                    User user = optUser.get();
                    return new FollowUserDTO(
                            user.getId(),
                            user.getUserName(),
                            user.getImage(),
                            user.getEmail()
                    );
                })
                .toList();
    }

    /**
     * Returns the number of users who follow the given user.
     */
    public long getFollowerCount(UUID userId) {
        long count = followRepository.countByFollowingId(userId);
        log.debug("Follower count for userId={} = {}", userId, count);
        return count;
    }

    /**
     * Returns the number of users the given user is following.
     */
    public long getFollowingCount(UUID userId) {
        long count = followRepository.countByFollowerId(userId);
        log.debug("Following count for userId={} = {}", userId, count);
        return count;
    }

}

