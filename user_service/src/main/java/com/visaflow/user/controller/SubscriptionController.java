package com.visaflow.user.controller;

import com.visaflow.user.dto.*;
import com.visaflow.user.service.SubscriptionService;
import com.visaflow.user.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public List<SubscriptionPlanDto> getAvailablePlans() {
        return subscriptionService.getAvailablePlans();
    }

    @GetMapping("/current")
    public SubscriptionResponse getCurrentSubscription() {
        return subscriptionService.getCurrentSubscription(SecurityUtils.getCurrentCompanyId());
    }

    @PostMapping("/current")
    public SubscriptionResponse createOrUpdateSubscription(@Valid @RequestBody SubscriptionCreateRequest request) {
        return subscriptionService.createOrUpdateSubscription(SecurityUtils.getCurrentCompanyId(), request);
    }

    @PostMapping("/cancel")
    public Map<String, Object> cancelSubscription() {
        return subscriptionService.cancelSubscription(SecurityUtils.getCurrentCompanyId());
    }
}
