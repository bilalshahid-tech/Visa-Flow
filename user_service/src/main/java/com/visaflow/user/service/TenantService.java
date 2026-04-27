package com.visaflow.user.service;

import com.visaflow.user.dto.*;
import com.visaflow.user.model.*;
import com.visaflow.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final CompanyRepository companyRepository;
    private final ProfileRepository profileRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public TenantStatsResponse getTenantStats(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Create CompanyResponse using constructor (record)
        CompanyResponse companyResp = new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getTaxId(),
                company.getEmail(),
                company.getPhone(),
                company.getAddress(),
                company.getWebsite(),
                company.getLogoUrl(),
                null, // subscription summary - can be populated later
                company.getStatus() != null ? company.getStatus().toString() : "ACTIVE",
                company.getCreatedAt(),
                company.getUpdatedAt(),
                null // stats - can be populated later
        );

        SubscriptionResponse subResp = subscriptionService.getCurrentSubscription(companyId);

        List<Profile> profiles = profileRepository.findByCompanyId(companyId);
        int totalUsers = profiles.size();

        return new TenantStatsResponse(
                companyResp,
                totalUsers,
                1,
                totalUsers,
                10,
                5,
                100,
                subResp,
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllTenants(String status, Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);
        return companies.map(company -> new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getTaxId(),
                company.getEmail(),
                company.getPhone(),
                company.getAddress(),
                company.getWebsite(),
                company.getLogoUrl(),
                null,
                company.getStatus() != null ? company.getStatus().toString() : "ACTIVE",
                company.getCreatedAt(),
                company.getUpdatedAt(),
                null
        ));
    }

    @Transactional(readOnly = true)
    public InternalCompanyValidationResponse validateCompany(UUID companyId, String feature) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyResponse companyResp = new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getTaxId(),
                company.getEmail(),
                company.getPhone(),
                company.getAddress(),
                company.getWebsite(),
                company.getLogoUrl(),
                null,
                company.getStatus() != null ? company.getStatus().toString() : "ACTIVE",
                company.getCreatedAt(),
                company.getUpdatedAt(),
                null
        );

        SubscriptionResponse subResp = subscriptionService.getCurrentSubscription(companyId);

        LimitsCheck check = new LimitsCheck(true, true, true);

        boolean isValid = subResp != null && "ACTIVE".equals(subResp.status());

        return new InternalCompanyValidationResponse(isValid, companyResp, subResp, check);
    }
}