package com.visaflow.user.repository;

import com.visaflow.user.model.TenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSettingsRepository extends JpaRepository<TenantSettings, UUID> {
    List<TenantSettings> findByCompanyId(UUID companyId);
    Optional<TenantSettings> findByCompanyIdAndSettingKey(UUID companyId, String settingKey);
}
