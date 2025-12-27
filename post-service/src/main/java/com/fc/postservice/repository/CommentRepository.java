package com.fc.postservice.repository;

import com.fc.postservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing Comment entities.
 * Provides helper methods for fetching top-level comments,
 * threaded replies, and batch comment count operations.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Fetch all top-level comments (parentComment IS NULL) for a given post.
     */
    List<Comment> findByPostIdAndParentCommentIsNull(UUID postId);

    /**
     * Fetch all replies/comments by parent comment ID.
     */
    List<Comment> findByParentCommentId(UUID parentId);

    /**
     * Count all comments (including replies) for a given post.
     */
    long countByPostId(UUID postId);

    /**
     * Count top-level comments (not including replies) for a post.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.parentComment IS NULL")
    long countTopLevelCommentsByPostId(@Param("postId") UUID postId);

    /**
     * Count comments grouped by post IDs.
     * Returns a list of Object[] { postId, count }.
     */
    @Query("SELECT c.postId AS postId, COUNT(c) AS cnt " +
            "FROM Comment c " +
            "WHERE c.postId IN :postIds " +
            "GROUP BY c.postId")
    List<Object[]> countCommentsByPostIds(@Param("postIds") Collection<UUID> postIds);

    /**
     * Convenience method: convert grouped result into Map<postId, count>.
     */
    default Map<UUID, Long> countMapByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = countCommentsByPostIds(postIds);
        return rows.stream().collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).longValue()
        ));
    }
}

