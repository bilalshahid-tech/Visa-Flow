package com.visaflow.user.repository;

import com.visaflow.user.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);
    List<Profile> findByCompanyId(UUID companyId);
    Page<Profile> findByCompanyId(UUID companyId, Pageable pageable);
    
    // Internal user bulk fetch
    List<Profile> findByUserIdIn(List<UUID> userIds);
}
