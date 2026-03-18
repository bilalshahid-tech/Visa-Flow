package com.visaflow.user.dto;

public record InternalCompanyValidationResponse(
    Boolean valid,
    CompanyResponse company,
    SubscriptionResponse subscription,
    LimitsCheck withinLimits
) {}
