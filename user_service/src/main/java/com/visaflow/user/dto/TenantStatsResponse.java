package com.visaflow.user.dto;

import java.util.List;

public record TenantStatsResponse(
    CompanyResponse company,
    Integer totalUsers,
    Integer activeToday,
    Integer activeThisWeek,
    Integer totalCases,
    Integer storageUsed,
    Integer apiCalls,
    SubscriptionResponse subscription,
    List<Object> recentActivities
) {}
