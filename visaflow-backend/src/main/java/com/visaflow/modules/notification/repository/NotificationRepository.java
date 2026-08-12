package com.visaflow.modules.notification.repository;

import com.visaflow.modules.notification.entity.Notification;
import com.visaflow.modules.notification.entity.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
