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
    private final CompanyService companyService;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public TenantStatsResponse getTenantStats(UUID companyId) {
        CompanyResponse companyResp = companyService.getMyCompany(companyId);
        SubscriptionResponse subResp = subscriptionService.getCurrentSubscription(companyId);

        List<Profile> profiles = profileRepository.findByCompanyId(companyId);
        int totalUsers = profiles.size();

        return new TenantStatsResponse(
            companyResp,
            totalUsers,
            1, // mocked active today
            totalUsers, // mocked active this week
            10, // mocked cases
            5, // mocked storage
            100, // mocked API calls
            subResp,
            List.of() // recent activities
        );
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllTenants(String status, Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable); // Could filter by status if applied
        return companies.map(companyService::getMyCompany); // Simplified for now since we just delegate
    }

    @Transactional(readOnly = true)
    public InternalCompanyValidationResponse validateCompany(UUID companyId, String feature) {
        CompanyResponse company = companyService.getMyCompany(companyId);
        SubscriptionResponse subResp = subscriptionService.getCurrentSubscription(companyId);
        
        // Simple mock of limits check
        LimitsCheck check = new LimitsCheck(true, true, true);
        
        // Validate feature logic could be expanded based on plan features
        boolean isValid = subResp != null && "ACTIVE".equals(subResp.status());
        if (feature != null && subResp != null) {
            SubscriptionFeatures features = subResp.plan().features();
            if ("apiAccess".equals(feature) && (features.apiAccess() == null || !features.apiAccess())) {
                isValid = false;
            }
        }
        
        return new InternalCompanyValidationResponse(isValid, company, subResp, check);
    }
}
