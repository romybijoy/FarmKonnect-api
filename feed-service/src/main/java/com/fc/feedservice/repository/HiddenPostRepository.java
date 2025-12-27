package com.fc.feedservice.repository;

import com.fc.feedservice.model.HiddenPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing HiddenPost entries.
 * This repository provides methods to check whether a post is hidden
 * by a user and to retrieve all hidden posts for a specific user.
 */
@Repository
public interface HiddenPostRepository extends JpaRepository<HiddenPost, UUID> {

    /**
     * Checks if a given post has been hidden by the specified user.
     *
     * @param userId ID of the user
     * @param postId ID of the post
     * @return true if the post is hidden by the user
     */
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    /**
     * Retrieves all posts hidden by a specific user.
     *
     * @param userId ID of the user
     * @return list of hidden post entries
     */
    List<HiddenPost> findByUserId(UUID userId);
}
