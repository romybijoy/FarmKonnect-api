package com.fc.postservice.repository;

import com.fc.postservice.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

public interface PostLikeRepository  extends JpaRepository<Like, UUID> {
//    int countByPostIdAndLikedTrue(UUID postId);
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
    void deleteByPostIdAndUserId(UUID postId, UUID userId);
    long countByPostId(UUID postId);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    @Query("SELECT l.post.id AS postId, COUNT(l) AS cnt " +
            "FROM Like l " +
            "WHERE l.post.id IN :postIds " +
            "GROUP BY l.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") Collection<UUID> postIds);

    default Map<UUID, Long> countMapByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = countLikesByPostIds(postIds);
        return rows.stream().collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).longValue()
        ));
    }
}
