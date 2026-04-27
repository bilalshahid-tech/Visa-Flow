package com.visaflow.cases.repository;

import com.visaflow.cases.domain.entity.Case;
import com.visaflow.cases.domain.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaseRepository extends JpaRepository<Case, UUID> {
    
    Optional<Case> findByCompanyIdAndApplicantId(UUID companyId, UUID applicantId);
    
    List<Case> findByCompanyId(UUID companyId);
    
    Optional<Case> findByCaseReference(String caseReference);
    
    List<Case> findByStatusAndCompanyId(CaseStatus status, UUID companyId);
}
