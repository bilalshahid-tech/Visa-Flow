package com.visaflow.user.service;

import com.visaflow.user.dto.*;
import com.visaflow.user.model.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DtoMapper {

    public CompanyResponse toCompanyResponse(Company company, SubscriptionSummary subSummary, CompanyStats stats) {
        if (company == null) return null;
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getTaxId(),
            company.getEmail(),
            company.getPhone(),
            company.getAddress(),
            company.getWebsite(),
            company.getLogoUrl(),
            subSummary,
            company.getStatus(),
            company.getCreatedAt(),
            company.getUpdatedAt(),
            stats
        );
    }

    public SubscriptionSummary toSubscriptionSummary(Subscription sub, SubscriptionPlan plan) {
        if (sub == null || plan == null) return null;
        return new SubscriptionSummary(
            sub.getId().toString(),
            plan.getId(),
            plan.getName(),
            sub.getStatus(),
            sub.getEndDate()
        );
    }

    public ProfileResponse toProfileResponse(Profile profile, Map<String, Object> authUserDetails) {
        if (profile == null) return null;
        return new ProfileResponse(
            profile.getId(),
            profile.getUserId(),
            profile.getCompanyId(),
            authUserDetails != null ? (String) authUserDetails.get("firstName") : null,
            authUserDetails != null ? (String) authUserDetails.get("lastName") : null,
            authUserDetails != null ? (String) authUserDetails.get("email") : null,
            authUserDetails != null ? (String) authUserDetails.get("role") : "CONSULTANT",
            profile.getPosition(),
            profile.getDepartment(),
            profile.getPhoneNumber(),
            profile.getAvatarUrl(),
            profile.getLanguage(),
            profile.getTimezone(),
            authUserDetails != null ? (Boolean) authUserDetails.get("emailVerified") : false,
            profile.getLastActive(),
            profile.getCreatedAt(),
            profile.getMetadata()
        );
    }
}
