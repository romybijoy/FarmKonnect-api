package com.fc.postservice.repository;


import com.fc.postservice.enums.AppealStatus;
import com.fc.postservice.model.Appeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
/**
 * Appeal Repository
 *
 * @author Romyb
 * @since 23/02/2026
 */


public interface AppealRepository extends JpaRepository<Appeal, UUID> {

    boolean existsByPostIdAndStatus(UUID postId, AppealStatus status);

    List<Appeal> findByStatus(AppealStatus status);

    Page<Appeal> findByUserId(UUID userId, Pageable pageable);

    Page<Appeal> findByStatus(AppealStatus status, Pageable pageable);
}
