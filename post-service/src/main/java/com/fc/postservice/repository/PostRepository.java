package com.fc.postservice.repository;

import com.fc.postservice.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUserIdIn(List<UUID> userIds);

    @EntityGraph(attributePaths = {"postImages"})
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds);

    // Explicit JPQL fetch-join to load postImages along with Post
    @Query("""
      select distinct p
      from Post p
      left join fetch p.postImages
      where p.userId in :userIds
      order by p.createdAt desc
    """)
    List<Post> findByUserIdInWithImages(@Param("userIds") List<UUID> userIds);
}
