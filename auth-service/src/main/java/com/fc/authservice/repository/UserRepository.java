package com.fc.authservice.repository;

import com.fc.authservice.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for performing CRUD and query operations on User entities.
 * Includes:
 * - Basic lookups (email, username, ID lists)
 * - Search capabilities
 * - Account status updates (block/unblock)
 * - Pagination queries
 * - Suggestion algorithm for recommended users
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email address.
     *
     * @param email user email
     * @return Optional containing the user or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Performs a keyword search for name, email, or role.
     * Note: Native query; ensure column names match DB schema.
     */
    @Query(value = "SELECT * FROM users p WHERE p.name LIKE %?1% OR p.email LIKE %?1% OR p.role=?2", nativeQuery = true)
    List<User> search(@Param("keyword") String keyword, @Param("role") String role);

    /**
     * Finds users where the role contains the given value.
     */
    List<User> findByRoleContaining(String role);

    /**
     * Updates block reason and disables a user.
     * Must be run inside a transaction.
     */
    @Transactional
    @Query(value = "UPDATE users p SET p.block_reason = ?1, p.enabled = false WHERE p.id = ?2", nativeQuery = true)
    @Modifying
    void updateBlockInfo(@Param("blockReason") String blockReason, @Param("userId") UUID userId);

    /**
     * Returns all enabled (active) users.
     */
    @Query(value = "SELECT * FROM users p WHERE p.enabled=true", nativeQuery = true)
    List<User> findAllByStatus();

    /**
     * Returns paginated users filtered by enabled status.
     */
    Page<User> findByEnabled(Boolean enabled, Pageable pageDetails);

    /**
     * Searches by username or email, case-insensitive, and filters by enabled status.
     */
    Page<User> findByUserNameOrEmailIgnoreCaseContainingAndEnabled(String keyword, String email, Boolean enabled, Pageable pageDetails);

    /**
     * Finds a user by exact username.
     */
    User findByUserName(String userName);

    /**
     * Finds users whose IDs are in a given list.
     */
    List<User> findByIdIn(List<UUID> ids);

    /**
     * Checks if an email exists (case-insensitive).
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Suggests possible users to follow, excluding:
     * - The current user (meId)
     * - Users already followed by the current user
     * Ordering logic:
     *  1. Users from the same district appear first.
     *  2. Then ordered by newest account creation (createdAt DESC).
     */
    @Query("""
        SELECT u
        FROM User u
        WHERE u.id <> :meId
          AND u.id NOT IN (
            SELECT f.followingId
            FROM FollowRelationship f
            WHERE f.followerId = :meId
          )
        ORDER BY
          CASE WHEN LOWER(u.district) = LOWER(:district) THEN 0 ELSE 1 END,
          u.createdAt DESC
        """)
    Page<User> findSuggestionCandidates(
            @Param("meId") UUID meId,
            @Param("district") String district,
            Pageable pageable
    );


    Long countByLastLoginAtAfter(LocalDateTime time);
}