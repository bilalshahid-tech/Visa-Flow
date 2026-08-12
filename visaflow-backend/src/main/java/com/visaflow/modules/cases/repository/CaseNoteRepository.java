package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.CaseNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNote, UUID> {
    List<CaseNote> findByVisaCaseIdOrderByCreatedAtAsc(UUID caseId);
}
