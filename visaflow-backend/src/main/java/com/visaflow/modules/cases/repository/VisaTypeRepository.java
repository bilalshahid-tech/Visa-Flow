package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.VisaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisaTypeRepository extends JpaRepository<VisaType, UUID> {
    Optional<VisaType> findByCode(String code);
}
