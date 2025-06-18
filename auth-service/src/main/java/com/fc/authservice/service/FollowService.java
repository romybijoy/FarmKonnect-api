package com.fc.authservice.service;

import com.fc.authservice.dto.FollowUserDTO;
import com.fc.authservice.model.FollowRelationship;
import com.fc.authservice.repository.FollowRepository;
import com.fc.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    public void followUser(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        if (alreadyFollowing) return;

        FollowRelationship relationship = new FollowRelationship();
        relationship.setFollowerId(followerId);
        relationship.setFollowingId(followingId);
        relationship.setFollowedAt(LocalDateTime.now());

        followRepository.save(relationship);
    }

    @Transactional
    public void unfollowUser(UUID followerId, UUID followingId) {
        FollowRelationship relationship = (FollowRelationship) followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("Relationship not found"));
        followRepository.delete(relationship);
    }

    public List<FollowUserDTO> getFollowers(UUID userId) {
        List<FollowRelationship> relationships = followRepository.findByFollowingId(userId);
        return relationships.stream()
                .map(rel -> userRepository.findById(rel.getFollowerId()))
                .filter(Optional::isPresent)
                .map(optUser -> {
                    var user = optUser.get();
                    return new FollowUserDTO(user.getId(), user.getUserName(), user.getImage());
                })
                .toList();
    }

    public List<FollowUserDTO> getFollowing(UUID userId) {
        List<FollowRelationship> relationships = followRepository.findByFollowerId(userId);
        return relationships.stream()
                .map(rel -> userRepository.findById(rel.getFollowingId()))
                .filter(Optional::isPresent)
                .map(optUser -> {
                    var user = optUser.get();
                    return new FollowUserDTO(user.getId(), user.getUserName(), user.getImage());
                })
                .toList();
    }

    public long getFollowerCount(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }

    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }

}

