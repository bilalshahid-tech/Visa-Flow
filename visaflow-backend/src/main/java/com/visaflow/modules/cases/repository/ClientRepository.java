package com.visaflow.modules.cases.repository;

import com.visaflow.modules.cases.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT c FROM Client c WHERE c.companyId = :companyId AND " +
           "(LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.passportNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Client> searchByCompany(@Param("companyId") UUID companyId,
                                 @Param("query") String query,
                                 Pageable pageable);

    boolean existsByCompanyIdAndPassportNumber(UUID companyId, String passportNumber);
}
