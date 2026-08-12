package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.DocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, UUID> {
    List<DocumentRequirement> findByVisaTypeIdOrderByDisplayOrderAsc(UUID visaTypeId);
}
