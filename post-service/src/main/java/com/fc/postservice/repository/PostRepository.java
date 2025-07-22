package com.fc.postservice.repository;

import com.fc.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUserIdIn(List<UUID> userIds);

    List<Post> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds);
}
