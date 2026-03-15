package com.visaflow.auth.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthEvent {
    private UUID userId;
    private String email;
    private String eventType; // USER_REGISTERED, USER_LOGGED_IN, PASSWORD_CHANGED, EMAIL_VERIFIED
    private Instant timestamp;
    private Map<String, Object> metadata;
}
