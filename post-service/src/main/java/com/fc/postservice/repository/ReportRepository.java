package com.fc.postservice.repository;

import com.fc.postservice.model.Report;
import com.fc.postservice.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing reports submitted on posts.
 * Supports filtering, searching by status, post, reporter, and
 * prevents duplicate active reports for the same post by the same user.
 */
public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

    /**
     * Fetch paginated list of reports filtered by their status.
     *
     * @param status   ReportStatus (PENDING, RESOLVED, REJECTED)
     * @param pageable Pagination details
     * @return Page of reports matching the criteria
     */
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Get all reports for a specific post.
     */
    List<Report> findByPostId(UUID postId);

    /**
     * Optional: Overrides default findById for clarity,
     * though JpaRepository already provides this method.
     */
    Optional<Report> findById(UUID id);

    /**
     * Prevents users from submitting multiple *active/pending* reports
     * on the same post.
     * Example usage:
     * existsByPostIdAndReporterIdAndStatus(postId, userId, PENDING)
     */
    boolean existsByPostIdAndReporterIdAndStatus(UUID postId, UUID reporterId, ReportStatus status);

    /**
     * Count number of reports with a specific status.
     * Useful for admin dashboard metrics.
     */
    long countByStatus(ReportStatus status);
}
