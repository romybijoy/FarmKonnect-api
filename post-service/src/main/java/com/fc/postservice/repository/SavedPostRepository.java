package com.fc.postservice.repository;

import com.fc.postservice.model.Save;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository handling CRUD operations for saved posts.
 * Supports existence checks, count queries, and batch aggregation for feed rendering.
 */
public interface SavedPostRepository  extends JpaRepository<Save, UUID> {

    // -------------------------------------------------------------------------
    //  Fetch Operations
    // -------------------------------------------------------------------------

    /**
     * Find a save entry for a given user and post.
     * Ensures each user can only save a post once.
     */
    Optional<Save> findByUserIdAndPostId(UUID userId, UUID postId);

    /**
     * Get all saved posts for a user.
     */
    List<Save> findByUserId(UUID userId);

    // -------------------------------------------------------------------------
    //  Existence Checks
    // -------------------------------------------------------------------------

    /**
     * Check if a given user has already saved a specific post.
     */
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    // -------------------------------------------------------------------------
    //  Counting
    // -------------------------------------------------------------------------

    /**
     * Count how many users have saved a given post.
     */
    long countByPostId(UUID postId);

    // -------------------------------------------------------------------------
    //  Delete
    // -------------------------------------------------------------------------

    /**
     * Unsave (remove) a saved entry for a user on a specific post.
     */
    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    // -------------------------------------------------------------------------
    //  Batch Aggregation (for Feed Service or bulk UI response)
    // -------------------------------------------------------------------------

    /**
     * Count saves for a list of posts.
     * Returns Object[] representing { postId, saveCount }.
     */
    @Query("SELECT s.post.id AS postId, COUNT(s) AS cnt " +
            "FROM Save s " +
            "WHERE s.post.id IN :postIds " +
            "GROUP BY s.post.id")
    List<Object[]> countSavesByPostIds(@Param("postIds") Collection<UUID> postIds);

    /**
     * Convert batch count result into Map<postId, count>.
     * Useful for feed-service aggregation.
     */
    default Map<UUID, Long> countMapByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = countSavesByPostIds(postIds);
        return rows.stream().collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).longValue()
        ));
    }
}
