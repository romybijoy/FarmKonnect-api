package com.fc.authservice.repository;

import com.fc.authservice.model.User;

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

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM users p WHERE p.name LIKE %?1% OR p.email LIKE %?1% OR p.role=?2", nativeQuery = true)
    List<User> search(@Param("keyword") String keyword, @Param("role") String role);

    List<User> findByRoleContaining(String role);

    @Transactional
    @Query(value = "UPDATE users p SET p.block_reason = ?1, p.enabled = false WHERE p.id = ?2", nativeQuery = true)
    @Modifying
    void updateBlockInfo(@Param("blockReason") String blockReason, @Param("userId") UUID userId);

    @Query(value = "SELECT * FROM users p WHERE p.enabled=true", nativeQuery = true)

    List<User> findAllByStatus();

    Page<User> findByEnabled(Boolean enabled, Pageable pageDetails);



    Page<User> findByUserNameOrEmailIgnoreCaseContainingAndEnabled(String keyword, String email, Boolean enabled, Pageable pageDetails);

    User findByUserName(String userName);

    List<User> findByIdIn(List<UUID> ids);

    boolean existsByEmailIgnoreCase(String email);
}