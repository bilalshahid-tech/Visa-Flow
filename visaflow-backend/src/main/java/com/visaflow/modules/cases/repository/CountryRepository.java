package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {
}
