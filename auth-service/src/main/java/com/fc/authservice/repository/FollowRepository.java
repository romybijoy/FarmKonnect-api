package com.fc.authservice.repository;

import com.fc.authservice.model.FollowRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<FollowRelationship, UUID> {

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    List<FollowRelationship> findByFollowingId(UUID userId); // followers

    List<FollowRelationship> findByFollowerId(UUID userId); // following

    long countByFollowingId(UUID userId); // Followers count

    long countByFollowerId(UUID userId);  // Following count

    Optional<Object> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}

