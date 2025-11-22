package com.fc.postservice.repository;

import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    // Explicit JPQL fetch-join to load postImages along with Post
    @Query("""
      select distinct p
      from Post p
      left join fetch p.postImages
      where p.userId in :userIds
      order by p.createdAt desc
    """)
    List<Post> findByUserIdInWithImages(@Param("userIds") List<UUID> userIds);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.status = :status, p.removedBy = :adminId, p.removedAt = CURRENT_TIMESTAMP, p.moderationReason = :reason WHERE p.id = :postId")
    void updateStatus(UUID postId, PostStatus status, UUID adminId, String reason);

}
