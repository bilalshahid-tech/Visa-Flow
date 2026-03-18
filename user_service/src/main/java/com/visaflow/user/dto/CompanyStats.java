package com.visaflow.user.dto;

public record CompanyStats(
    Integer totalUsers,
    Integer totalCases,
    Integer totalConsultants,
    Integer totalClients
) {}
