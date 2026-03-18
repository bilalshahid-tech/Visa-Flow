package com.visaflow.user.repository;

import com.visaflow.user.model.UserAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, UUID> {
    Page<UserAuditLog> findByCompanyId(UUID companyId, Pageable pageable);
    Page<UserAuditLog> findByUserId(UUID userId, Pageable pageable);
}
