package com.visaflow.cases.repository;

import com.visaflow.cases.domain.entity.CaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, UUID> {

    @Query("SELECT h FROM CaseStatusHistory h WHERE h.visaCase.id = :caseId ORDER BY h.changedAt DESC")
    List<CaseStatusHistory> findByCaseIdOrderByChangedAtDesc(@Param("caseId") UUID caseId);
}
