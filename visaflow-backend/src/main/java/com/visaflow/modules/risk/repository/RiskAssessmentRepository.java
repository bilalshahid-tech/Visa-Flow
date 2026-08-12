package com.visaflow.modules.risk.repository;

import com.visaflow.modules.risk.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    Optional<RiskAssessment> findTopByCaseIdAndCompanyIdOrderByCreatedAtDesc(UUID caseId, UUID companyId);
}
