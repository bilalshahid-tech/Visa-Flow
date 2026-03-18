package com.visaflow.user.repository;

import com.visaflow.user.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByCompanyIdAndStatus(UUID companyId, String status);
    Optional<Subscription> findFirstByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
