package com.fc.postservice.repository;

import com.fc.postservice.model.Save;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedPostRepository  extends JpaRepository<Save, UUID> {

    Optional<Save> findByUserIdAndPostId(UUID userId, UUID postId);
//
//    void deleteByIdUserIdAndIdPostId(UUID userId, UUID postId);

    List<Save> findByUserId(UUID userId);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    long countByPostId(UUID postId);

    void deleteByUserIdAndPostId(UUID userId, UUID postId);

}
