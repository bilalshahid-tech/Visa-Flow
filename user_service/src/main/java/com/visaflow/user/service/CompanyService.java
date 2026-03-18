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

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ProfileRepository profileRepository;
    private final AuthServiceClient authServiceClient;
    private final SubscriptionService subscriptionService;
    private final KafkaEventProducer kafkaEventProducer;
    private final DtoMapper dtoMapper;

    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest req) {
        log.info("Creating new company: {}", req.name());
        
        if (companyRepository.findByEmail(req.email()).isPresent()) {
            throw new RuntimeException("Company with email " + req.email() + " already exists");
        }

        Company company = Company.builder()
                .name(req.name())
                .email(req.email())
                .taxId(req.taxId())
                .phone(req.phone())
                .address(req.address())
                .website(req.website())
                .logoUrl(req.logo())
                .timezone(req.timezone())
                .language(req.language())
                .status("ACTIVE")
                .build();
                
        company = companyRepository.save(company);

        // Call Auth Service to create the admin user
        UUID adminUserId = authServiceClient.createUser(req.email(), req.name() + " Admin", "ADMIN", company.getId());

        // Create initial default subscription
        subscriptionService.createDefaultSubscription(company.getId());

        // Create Admin Profile
        Profile adminProfile = Profile.builder()
                .userId(adminUserId)
                .companyId(company.getId())
                .language(req.language() != null ? req.language() : "en")
                .timezone(req.timezone() != null ? req.timezone() : "UTC")
                .build();
        profileRepository.save(adminProfile);

        // Publish event
        kafkaEventProducer.publishCompanyCreated(company.getId());

        return getCompanyResponse(company);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getMyCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return getCompanyResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (req.name() != null) company.setName(req.name());
        if (req.phone() != null) company.setPhone(req.phone());
        if (req.address() != null) company.setAddress(req.address());
        if (req.website() != null) company.setWebsite(req.website());
        if (req.logo() != null) company.setLogoUrl(req.logo());
        if (req.timezone() != null) company.setTimezone(req.timezone());
        if (req.language() != null) company.setLanguage(req.language());

        company = companyRepository.save(company);
        return getCompanyResponse(company);
    }

    @Transactional(readOnly = true)
    public Page<ProfileResponse> getCompanyUsers(UUID companyId, String role, Pageable pageable) {
        Page<Profile> profiles = profileRepository.findByCompanyId(companyId, pageable);
        return profiles.map(p -> {
            Map<String, Object> authDetails = authServiceClient.fetchUserDetails(p.getUserId());
            return dtoMapper.toProfileResponse(p, authDetails);
        });
    }

    private CompanyResponse getCompanyResponse(Company company) {
        SubscriptionResponse subDetails = subscriptionService.getCurrentSubscription(company.getId());
        SubscriptionPlanDto planDto = subDetails.plan();
        SubscriptionSummary summary = new SubscriptionSummary(
                subDetails.id().toString(), planDto.id(), planDto.name(), subDetails.status(), subDetails.endDate());
                
        // Mock stats for now
        CompanyStats stats = new CompanyStats(1, 0, 1, 0); 
        return dtoMapper.toCompanyResponse(company, summary, stats);
    }
}
