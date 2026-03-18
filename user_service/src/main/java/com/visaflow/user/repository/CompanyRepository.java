package com.visaflow.user.repository;

import com.visaflow.user.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByEmail(String email);
    Optional<Company> findByTaxId(String taxId);
}
