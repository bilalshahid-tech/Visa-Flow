package com.visaflow.user.controller;

import com.visaflow.user.dto.*;
import com.visaflow.user.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/admin/tenants")
@RequiredArgsConstructor
public class AdminController {

    private final TenantService tenantService;

    @GetMapping("/{companyId}/stats")
    public TenantStatsResponse getTenantStats(@PathVariable UUID companyId) {
        return tenantService.getTenantStats(companyId);
    }

    @GetMapping
    public Page<CompanyResponse> getAllTenants(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return tenantService.getAllTenants(status, pageable);
    }
}
