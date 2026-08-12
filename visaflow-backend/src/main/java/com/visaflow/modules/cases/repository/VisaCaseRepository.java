package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.VisaCase;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisaCaseRepository extends JpaRepository<VisaCase, UUID> {

    Page<VisaCase> findByCompanyId(UUID companyId, Pageable pageable);

    Page<VisaCase> findByCompanyIdAndStatus(UUID companyId, CaseStatus status, Pageable pageable);

    Optional<VisaCase> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCaseReference(String caseReference);

    @Query("SELECT v FROM VisaCase v LEFT JOIN FETCH v.client LEFT JOIN FETCH v.visaType " +
           "WHERE v.id = :id AND v.companyId = :companyId")
    Optional<VisaCase> findDetailedByIdAndCompanyId(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
