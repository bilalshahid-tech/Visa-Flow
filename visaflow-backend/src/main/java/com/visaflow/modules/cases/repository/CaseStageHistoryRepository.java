package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.CaseStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseStageHistoryRepository extends JpaRepository<CaseStageHistory, UUID> {

    @Query("SELECT h FROM CaseStageHistory h WHERE h.visaCase.id = :caseId ORDER BY h.enteredAt ASC")
    List<CaseStageHistory> findByCaseVisaCaseIdOrderByEnteredAtAsc(@Param("caseId") UUID caseId);

    @Query("SELECT h FROM CaseStageHistory h WHERE h.visaCase.id = :caseId AND h.stage.id = :stageId AND h.completedAt IS NULL")
    Optional<CaseStageHistory> findByCaseVisaCaseIdAndStageIdAndCompletedAtIsNull(
            @Param("caseId") UUID caseId, @Param("stageId") UUID stageId);
}
