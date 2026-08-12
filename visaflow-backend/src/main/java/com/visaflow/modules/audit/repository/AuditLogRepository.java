package com.visaflow.modules.audit.repository;

import com.visaflow.modules.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.companyId = :companyId AND a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByEntityOrderByCreatedAtDesc(
            @Param("companyId") UUID companyId,
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId,
            Pageable pageable);
}
