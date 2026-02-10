package com.fc.authservice.repository;

import com.fc.authservice.model.FollowRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing FollowRelationship entities.
 * Provides derived query methods for checking follow status, listing
 * followers/following, and counting follower metrics. Also contains
 * a custom JPQL query for mutual follow (friends) calculation.
 */
@Repository
public interface FollowRepository extends JpaRepository<FollowRelationship, UUID> {

    /**
     * Checks if a follow relationship already exists.
     *
     * @param followerId  ID of the follower
     * @param followingId ID of the user being followed
     * @return true if the relationship exists
     */
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * Deletes an existing follow relationship.
     */
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * Retrieves users who follow the given user.
     * Equivalent to: "followers of this user"
     *
     * @param userId ID of the target user
     * @return list of FollowRelationship entries
     */
    List<FollowRelationship> findByFollowingId(UUID userId); // followers

    /**
     * Retrieves users whom the given user is following.
     * Equivalent to: "users this user follows"
     *
     * @param userId ID of the follower
     * @return list of FollowRelationship entries
     */
    List<FollowRelationship> findByFollowerId(UUID userId); // following

    /**
     * Counts how many users are following the given user.
     *
     * @param userId target user ID
     * @return number of followers
     */
    long countByFollowingId(UUID userId); // Followers count

    /**
     * Counts how many users the given user is following.
     *
     * @param userId follower user ID
     * @return number of users followed
     */
    long countByFollowerId(UUID userId);  // Following count

    /**
     * Retrieves a follow relationship if it exists between two users.
     *
     * @return Optional containing the relationship or empty if not found
     */
    Optional<Object> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * Counts the number of "mutual follow sources" between two users.
     * This query checks:
     * - f1: people *I follow*
     * - f2: people *those users also follow*
     * Essentially: shared follow targets = mutual connections
     * Example:
     * If I follow A and B,
     * and A or B also follow the "otherUser",
     * count them.
     *
     * @param meId         The ID of the current user
     * @param otherUserId  The other user ID to compare follow relationships with
     * @return number of mutual follow sources
     */
    @Query("""
        SELECT COUNT(f2)
        FROM FollowRelationship f1
        JOIN FollowRelationship f2
          ON f1.followingId = f2.followerId
        WHERE f1.followerId = :meId
          AND f2.followingId = :otherUserId
        """)
    long countMutualFollowSources(
            @Param("meId") UUID meId,
            @Param("otherUserId") UUID otherUserId
    );
}

