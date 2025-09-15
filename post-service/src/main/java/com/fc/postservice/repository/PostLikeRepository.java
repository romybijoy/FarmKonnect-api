package com.fc.postservice.repository;

import com.fc.postservice.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostLikeRepository  extends JpaRepository<Like, UUID> {
//    int countByPostIdAndLikedTrue(UUID postId);
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
    void deleteByPostIdAndUserId(UUID postId, UUID userId);
    long countByPostId(UUID postId);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
    void deleteByUserIdAndPostId(UUID userId, UUID postId);
}
