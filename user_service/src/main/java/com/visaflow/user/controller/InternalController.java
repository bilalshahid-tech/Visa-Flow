package com.visaflow.user.controller;

import com.visaflow.user.dto.*;
import com.visaflow.user.service.ProfileService;
import com.visaflow.user.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalController {

    private final TenantService tenantService;
    private final ProfileService profileService;

    @PostMapping("/companies/validate")
    public InternalCompanyValidationResponse validateCompany(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody InternalCompanyValidationRequest request) {
        return tenantService.validateCompany(request.companyId(), request.requiredFeature());
    }

    @PostMapping("/profiles/batch")
    public List<ProfileResponse> getProfilesBatch(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestBody Map<String, List<UUID>> request) {
        List<UUID> userIds = request.get("userIds");
        return profileService.batchGetProfiles(userIds);
    }
}
