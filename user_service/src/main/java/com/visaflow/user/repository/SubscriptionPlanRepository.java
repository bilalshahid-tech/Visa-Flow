package com.visaflow.user.repository;

import com.visaflow.user.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    List<SubscriptionPlan> findByIsActiveTrue();
}
