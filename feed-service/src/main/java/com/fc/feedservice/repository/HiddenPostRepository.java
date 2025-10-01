package com.fc.feedservice.repository;

import com.fc.feedservice.model.HiddenPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HiddenPostRepository extends JpaRepository<HiddenPost, UUID> {
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
    List<HiddenPost> findByUserId(UUID userId);
}
