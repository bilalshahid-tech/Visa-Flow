package com.visaflow.user.repository;

import com.visaflow.user.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByEmailAndCompanyIdAndStatus(String email, UUID companyId, String status);
}
