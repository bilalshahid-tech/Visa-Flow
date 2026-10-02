package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.StageDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StageDocumentRequirementRepository extends JpaRepository<StageDocumentRequirement, UUID> {

    List<StageDocumentRequirement> findByStageIdOrderByDisplayOrderAsc(UUID stageId);
}
