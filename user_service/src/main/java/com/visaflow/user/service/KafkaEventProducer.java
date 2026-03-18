package com.visaflow.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompanyCreated(UUID companyId) {
        log.info("Publishing COMPANY_CREATED event for company: {}", companyId);
        kafkaTemplate.send("company-events", companyId.toString(), "COMPANY_CREATED");
    }

    public void publishProfileUpdated(UUID profileId) {
        log.info("Publishing PROFILE_UPDATED event for profile: {}", profileId);
        kafkaTemplate.send("profile-events", profileId.toString(), "PROFILE_UPDATED");
    }

    public void publishSubscriptionChanged(UUID companyId, String planId) {
        log.info("Publishing SUBSCRIPTION_CHANGED event for company: {}, new plan: {}", companyId, planId);
        kafkaTemplate.send("subscription-events", companyId.toString(), "SUBSCRIPTION_CHANGED:" + planId);
    }
}
