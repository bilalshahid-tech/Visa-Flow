package com.visaflow.user.dto;

public record SubscriptionFeatures(
    Integer maxUsers,
    Integer maxCases,
    Integer maxDocuments,
    Integer storageGB,
    Boolean apiAccess,
    Boolean advancedAnalytics,
    Boolean prioritySupport,
    Boolean customBranding
) {}
