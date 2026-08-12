package com.visaflow.modules.document.repository;

import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.entity.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findByCaseIdAndCompanyId(UUID caseId, UUID companyId, Pageable pageable);
    List<Document> findByCaseIdAndCompanyIdAndStatus(UUID caseId, UUID companyId, DocumentStatus status);
    Optional<Document> findByIdAndCompanyId(UUID id, UUID companyId);
}
