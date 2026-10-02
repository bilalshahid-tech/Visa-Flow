package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.VisaProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VisaProgramRepository extends JpaRepository<VisaProgram, UUID> {

    @Query("SELECT p FROM VisaProgram p JOIN FETCH p.country JOIN FETCH p.visaCategory WHERE p.active = true ORDER BY p.name")
    List<VisaProgram> findAllActive();

    @Query("SELECT p FROM VisaProgram p JOIN FETCH p.country JOIN FETCH p.visaCategory WHERE p.country.id = :countryId AND p.active = true ORDER BY p.name")
    List<VisaProgram> findByCountryIdAndActiveTrue(@Param("countryId") UUID countryId);
}
