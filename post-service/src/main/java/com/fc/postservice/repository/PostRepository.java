package com.fc.postservice.repository;

import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Post entity with support for:
 * - Fetch feed posts with images
 * - Moderation updates (remove, block, restore posts)
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Fetch posts created by a list of users, including the image URLs.
     * Uses DISTINCT because fetch-join + element collection creates duplicates.
     * NOTE: Sorting in JPQL with fetch join may not always be stable.
     *       If needed, sort via Java after fetching.
     */
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds);

    /**
     * Update moderation fields for a post (status, removedBy, removedAt, reason).
     */
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.status = :status, p.removedBy = :adminId, p.removedAt = CURRENT_TIMESTAMP, p.moderationReason = :reason WHERE p.id = :postId")
    void updateStatus(UUID postId, PostStatus status, UUID adminId, String reason);

/* fetch post based on user id with pagination */
    Page<Post> findByUserId(UUID userId, Pageable pageable);
    
    List<Post> findByStatus(PostStatus status);

}
