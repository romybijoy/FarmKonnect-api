package com.fc.postservice.repository;

import com.fc.postservice.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Comment Like Repository
 *
 * @author Romyb
 * @since 20/02/2026
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    Optional<CommentLike> findByCommentIdAndUserId(UUID commentId, UUID userId);

    @Query("""
        SELECT cl.comment.id
        FROM CommentLike cl
        WHERE cl.userId = :userId
    """)
    Set<UUID> findLikedCommentIdsByUser(UUID userId);

    @Query("""
    SELECT cl.comment.id
    FROM CommentLike cl
    WHERE cl.userId = :userId
      AND cl.comment.postId = :postId
""")
    Set<UUID> findLikedCommentIdsByUserAndPost(UUID userId, UUID postId);

}
