package com.fc.stories_service.repository;

import com.fc.stories_service.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {

    List<Story> findByUserIdAndExpiresAtAfter(UUID userId, LocalDateTime expiresAt);
    List<Story> findByExpiresAtAfter(LocalDateTime now);

    // 🔍 Finds all stories where userId is in the list and the story hasn't expired
    List<Story> findByUserIdInAndExpiresAtAfter(List<UUID> userIds, LocalDateTime currentTime);

}
