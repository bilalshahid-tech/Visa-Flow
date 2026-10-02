package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.VisaProgramStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisaProgramStageRepository extends JpaRepository<VisaProgramStage, UUID> {

    List<VisaProgramStage> findByVisaProgramIdOrderBySequenceOrderAsc(UUID visaProgramId);

    Optional<VisaProgramStage> findFirstByVisaProgramIdOrderBySequenceOrderAsc(UUID visaProgramId);

    @Query("SELECT s FROM VisaProgramStage s WHERE s.visaProgram.id = :programId AND s.sequenceOrder = :order")
    Optional<VisaProgramStage> findByProgramIdAndOrder(@Param("programId") UUID programId, @Param("order") int order);
}
