package com.fc.postservice.repository;

import com.fc.postservice.model.PostLike;
import com.fc.postservice.model.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostLikeRepository  extends JpaRepository<PostLike, PostLikeId> {
//    int countByPostIdAndLikedTrue(UUID postId);
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
    void deleteByPostIdAndUserId(UUID postId, UUID userId);
    long countByPostId(UUID postId);
}
