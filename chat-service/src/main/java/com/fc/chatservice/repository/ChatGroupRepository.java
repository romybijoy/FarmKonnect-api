package com.fc.chatservice.repository;

import com.fc.chatservice.model.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, UUID> {

    // Fetch group and members in one query to avoid lazy-loading problems
    @Query("select g from ChatGroup g left join fetch g.members m where g.id = :id")
    Optional<ChatGroup> findByIdWithMembers(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT g
        FROM ChatGroup g
        JOIN g.members m
        WHERE m.userId = :userId
    """)
    List<ChatGroup> findAllByUserId(UUID userId);
}
