package com.fc.postservice.repository;

import com.fc.postservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostIdAndParentCommentIsNull(UUID postId);
    List<Comment> findByParentCommentId(UUID parentId);
    long countByPostId(UUID postId);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.parentComment IS NULL")
    long countTopLevelCommentsByPostId(@Param("postId") UUID postId);

    @Query("SELECT c.postId AS postId, COUNT(c) AS cnt " +
            "FROM Comment c " +
            "WHERE c.postId IN :postIds " +
            "GROUP BY c.postId")
    List<Object[]> countCommentsByPostIds(@Param("postIds") Collection<UUID> postIds);

    // convenience default to map
    default Map<UUID, Long> countMapByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = countCommentsByPostIds(postIds);
        return rows.stream().collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).longValue()
        ));
    }
}

