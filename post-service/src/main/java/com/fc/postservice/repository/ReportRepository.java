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

public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    List<Report> findByPostId(UUID postId);

    Optional<Report> findById(UUID id);

    boolean existsByPostIdAndReporterIdAndStatus(UUID postId, UUID reporterId, ReportStatus status);

    long countByStatus(ReportStatus status);
}
