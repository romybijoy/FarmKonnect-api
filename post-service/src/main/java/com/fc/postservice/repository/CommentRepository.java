package com.fc.postservice.repository;

import com.fc.postservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostIdAndParentCommentIsNull(UUID postId);
    List<Comment> findByParentCommentId(UUID parentId);
    long countByPostId(UUID postId);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.parentComment IS NULL")
    long countTopLevelCommentsByPostId(@Param("postId") UUID postId);
}

