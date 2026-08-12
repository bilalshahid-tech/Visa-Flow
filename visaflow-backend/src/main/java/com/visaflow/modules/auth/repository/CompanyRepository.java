package com.visaflow.modules.auth.repository;

import com.visaflow.modules.auth.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
