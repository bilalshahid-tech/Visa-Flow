package com.visaflow.user.controller;

import com.visaflow.user.dto.*;
import com.visaflow.user.service.CompanyService;
import com.visaflow.user.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return companyService.createCompany(request);
    }

    @GetMapping("/my")
    public CompanyResponse getMyCompany() {
        return companyService.getMyCompany(SecurityUtils.getCurrentCompanyId());
    }

    @PutMapping("/my")
    public CompanyResponse updateMyCompany(@Valid @RequestBody CompanyUpdateRequest request) {
        return companyService.updateCompany(SecurityUtils.getCurrentCompanyId(), request);
    }

    @GetMapping("/{companyId}/users")
    public Page<ProfileResponse> getCompanyUsers(
            @PathVariable UUID companyId,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        // In reality, might filter by role and implement proper security checks
        return companyService.getCompanyUsers(companyId, role, pageable);
    }
}
