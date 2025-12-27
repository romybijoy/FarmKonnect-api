package com.fc.postservice.repository;

import com.fc.postservice.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for handling post-like operations.
 * Supports existence checks, like counts, and batch count retrieval.
 */
public interface PostLikeRepository  extends JpaRepository<Like, UUID> {

    // -------------------------------------------------------------------------
    //  Existence checks
    // -------------------------------------------------------------------------

    /**
     * Check if the user has already liked a specific post.
     */
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    /**
     * Same as above but reversed argument order (if needed by service logic).
     */
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    // -------------------------------------------------------------------------
    //  Delete like
    // -------------------------------------------------------------------------

    /**
     * Remove a like entry based on post and user ID.
     */
    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    /**
     * Same as above but reversed parameter order.
     */
    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    // -------------------------------------------------------------------------
    //  Count likes
    // -------------------------------------------------------------------------

    /**
     * Count number of likes on a single post.
     */
    long countByPostId(UUID postId);

    /**
     * Batch count likes for multiple posts.
     * Returns list of Object[] → { postId, likeCount }
     */
    @Query("SELECT l.post.id AS postId, COUNT(l) AS cnt " +
            "FROM Like l " +
            "WHERE l.post.id IN :postIds " +
            "GROUP BY l.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") Collection<UUID> postIds);

    /**
     * Convenience method that converts JPQL results into a Map<postId, count>.
     */
    default Map<UUID, Long> countMapByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = countLikesByPostIds(postIds);
        return rows.stream().collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).longValue()
        ));
    }
}
