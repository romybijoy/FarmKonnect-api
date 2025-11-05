package com.fc.postservice.repository;

import com.fc.postservice.model.Save;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

public interface SavedPostRepository  extends JpaRepository<Save, UUID> {

    Optional<Save> findByUserIdAndPostId(UUID userId, UUID postId);
//
//    void deleteByIdUserIdAndIdPostId(UUID userId, UUID postId);

    List<Save> findByUserId(UUID userId);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    long countByPostId(UUID postId);

    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    @Query("SELECT s.post.id AS postId, COUNT(s) AS cnt " +
            "FROM Save s " +
            "WHERE s.post.id IN :postIds " +
            "GROUP BY s.post.id")
    List<Object[]> countSavesByPostIds(@Param("postIds") Collection<UUID> postIds);

    default Map<UUID, Long> countMapByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = countSavesByPostIds(postIds);
        return rows.stream().collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).longValue()
        ));
    }
}
