package com.visaflow.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;
    private static final String TOPIC = "auth-events";

    public void sendAuthEvent(com.visaflow.auth.model.User user, String eventType, Map<String, Object> metadata) {
        AuthEvent event = AuthEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .eventType(eventType)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();

        log.info("Publishing auth event: {} for user: {}", eventType, user.getEmail());
        try {
            kafkaTemplate.send(TOPIC, user.getId().toString(), event);
        } catch (Exception e) {
            log.warn("Failed to send auth event to Kafka (ensure broker is running): {}", e.getMessage());
        }
    }
}
