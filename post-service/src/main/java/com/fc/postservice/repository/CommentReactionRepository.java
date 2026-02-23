package com.fc.postservice.repository;

import com.fc.postservice.model.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Comment Reaction Repository
 *
 * @author Romyb
 * @since 20/02/2026
 */
public interface CommentReactionRepository
        extends JpaRepository<CommentReaction, UUID> {

    Optional<CommentReaction> findByCommentIdAndUserId(UUID commentId, UUID userId);

    @Query("""
    SELECT r.comment.id,
           r.emoji,
           COUNT(r),
           SUM(CASE WHEN r.userId = :userId THEN 1 ELSE 0 END)
    FROM CommentReaction r
    WHERE r.comment.id IN :commentIds
    GROUP BY r.comment.id, r.emoji
""")
    List<Object[]> aggregateReactionsForComments(
            @Param("commentIds") List<UUID> commentIds,
            @Param("userId") UUID userId
    );

}