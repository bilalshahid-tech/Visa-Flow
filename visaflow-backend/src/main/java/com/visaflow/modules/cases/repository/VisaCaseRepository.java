package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.VisaCase;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisaCaseRepository extends JpaRepository<VisaCase, UUID> {

    /**
     * List all cases for a company — JOIN FETCHes client + visaType/visaProgram
     * to prevent LazyInitializationException when mapping to CaseResponse.
     * The count query is kept simple (no fetches) for pagination performance.
     */
    @Query(value = "SELECT v FROM VisaCase v "
            + "LEFT JOIN FETCH v.client "
            + "LEFT JOIN FETCH v.visaType "
            + "LEFT JOIN FETCH v.visaProgram "
            + "WHERE v.companyId = :companyId",
           countQuery = "SELECT COUNT(v) FROM VisaCase v WHERE v.companyId = :companyId")
    Page<VisaCase> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query(value = "SELECT v FROM VisaCase v "
            + "LEFT JOIN FETCH v.client "
            + "LEFT JOIN FETCH v.visaType "
            + "LEFT JOIN FETCH v.visaProgram "
            + "WHERE v.companyId = :companyId AND v.status = :status",
           countQuery = "SELECT COUNT(v) FROM VisaCase v WHERE v.companyId = :companyId AND v.status = :status")
    Page<VisaCase> findByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") CaseStatus status, Pageable pageable);

    Optional<VisaCase> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCaseReference(String caseReference);

    @Query("SELECT v FROM VisaCase v "
            + "LEFT JOIN FETCH v.client "
            + "LEFT JOIN FETCH v.visaType "
            + "LEFT JOIN FETCH v.visaProgram vp "
            + "LEFT JOIN FETCH vp.visaCategory "
            + "LEFT JOIN FETCH v.currentStage cs "
            + "LEFT JOIN FETCH cs.visaProgram "
            + "WHERE v.id = :id AND v.companyId = :companyId")
    Optional<VisaCase> findDetailedByIdAndCompanyId(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("SELECT v FROM VisaCase v WHERE v.client.id = :clientId AND v.companyId = :companyId ORDER BY v.createdAt DESC")
    List<VisaCase> findByClientIdAndCompanyIdOrderByCreatedAtDesc(@Param("clientId") UUID clientId, @Param("companyId") UUID companyId);
}
