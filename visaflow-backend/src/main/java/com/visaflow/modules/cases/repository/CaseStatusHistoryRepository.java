package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.CaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, UUID> {
    List<CaseStatusHistory> findByVisaCaseIdOrderByChangedAtAsc(UUID caseId);
}
