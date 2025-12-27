package com.fc.stories_service.repository;

import com.fc.stories_service.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing and querying Story entities.
 * Provides methods to fetch active (non-expired) stories by user or user groups.
 */
@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {

    /**
     * Finds all active (non-expired) stories for a specific user.
     *
     * @param userId       the ID of the user whose stories should be retrieved
     * @param expiresAt    reference time; normally LocalDateTime.now()
     * @return list of the user's active stories
     */
    List<Story> findByUserIdAndExpiresAtAfter(UUID userId, LocalDateTime expiresAt);

    /**
     * Retrieves all active (non-expired) stories in the system.
     *
     * @param now timestamp for filtering; normally LocalDateTime.now()
     * @return list of all active stories
     */
    List<Story> findByExpiresAtAfter(LocalDateTime now);

    /**
     * Fetches active stories from multiple users.
     * Commonly used for:
     * - stories from users the requester follows
     * - combined feed containing user + followers
     *
     * @param userIds     list of users whose stories should be fetched
     * @param currentTime reference time; normally LocalDateTime.now()
     * @return list of active stories from the given userIds
     */
    List<Story> findByUserIdInAndExpiresAtAfter(List<UUID> userIds, LocalDateTime currentTime);

}
