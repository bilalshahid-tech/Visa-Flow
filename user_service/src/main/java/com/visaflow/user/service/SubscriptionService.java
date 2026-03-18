package com.visaflow.user.service;

import com.visaflow.user.dto.*;
import com.visaflow.user.model.*;
import com.visaflow.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanDto> getAvailablePlans() {
        return planRepository.findByIsActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(UUID companyId) {
        Subscription sub = subscriptionRepository.findFirstByCompanyIdOrderByCreatedAtDesc(companyId)
                .orElseThrow(() -> new RuntimeException("No subscription found for company " + companyId));
        SubscriptionPlan plan = planRepository.findById(sub.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return toResponse(sub, plan);
    }

    @Transactional
    public void createDefaultSubscription(UUID companyId) {
        log.info("Creating default subscription for company: {}", companyId);
        SubscriptionPlan freePlan = planRepository.findById("free")
                .orElseThrow(() -> new RuntimeException("Free plan not found in database"));
        
        Subscription sub = Subscription.builder()
                .companyId(companyId)
                .planId(freePlan.getId())
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .autoRenew(true)
                .build();
                
        subscriptionRepository.save(sub);
    }

    @Transactional
    public SubscriptionResponse createOrUpdateSubscription(UUID companyId, SubscriptionCreateRequest req) {
        SubscriptionPlan plan = planRepository.findById(req.planId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + req.planId()));

        Subscription existing = subscriptionRepository.findFirstByCompanyIdOrderByCreatedAtDesc(companyId)
                .orElse(null);

        if (existing != null) {
            existing.setStatus("CANCELED");
            existing.setCancelledAt(LocalDateTime.now());
            subscriptionRepository.save(existing);
        }

        Subscription newSub = Subscription.builder()
                .companyId(companyId)
                .planId(plan.getId())
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .endDate(req.interval().equalsIgnoreCase("yearly") ? LocalDateTime.now().plusYears(1) : LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .build();

        newSub = subscriptionRepository.save(newSub);
        kafkaEventProducer.publishSubscriptionChanged(companyId, plan.getId());
        
        return toResponse(newSub, plan);
    }

    @Transactional
    public Map<String, Object> cancelSubscription(UUID companyId) {
        Subscription sub = subscriptionRepository.findFirstByCompanyIdOrderByCreatedAtDesc(companyId)
                .orElseThrow(() -> new RuntimeException("Active subscription not found"));
        
        sub.setAutoRenew(false);
        sub.setStatus("CANCELED");
        sub.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(sub);
        
        kafkaEventProducer.publishSubscriptionChanged(companyId, "canceled");
        
        return Map.of(
            "message", "Subscription cancelled successfully", 
            "effectiveDate", sub.getCancelledAt().toString()
        );
    }

    // Helper mappers
    private SubscriptionPlanDto toDto(SubscriptionPlan p) {
        // Simple manual map to handle features JSON
        return new SubscriptionPlanDto(
            p.getId(), p.getName(), p.getPrice(), p.getCurrency(), p.getIntervalType(),
            new SubscriptionFeatures(p.getMaxUsers(), p.getMaxCases(), p.getMaxDocuments(), 
                                     p.getStorageGb(), p.getApiAccess(), p.getAdvancedAnalytics(), 
                                     p.getPrioritySupport(), p.getCustomBranding())
        );
    }

    private SubscriptionResponse toResponse(Subscription s, SubscriptionPlan p) {
        return new SubscriptionResponse(
            s.getId(), s.getCompanyId(), toDto(p), s.getStatus(), 
            s.getStartDate(), s.getEndDate(), s.getAutoRenew(), 
            s.getTrialEndsAt(), s.getCancelledAt(), s.getPaymentMethod(), List.of()
        );
    }
}
