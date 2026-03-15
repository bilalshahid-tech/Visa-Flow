package com.visaflow.auth.service;

import com.visaflow.auth.model.EmailVerificationToken;
import com.visaflow.auth.model.User;
import com.visaflow.auth.repository.EmailVerificationTokenRepository;
import com.visaflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final com.visaflow.auth.kafka.AuthEventProducer eventProducer;

    @Transactional
    public void createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(86400)) // 24 hours
                .build();
        tokenRepository.save(verificationToken);

        log.info("Created verification token for user: {}. Token: {}", user.getEmail(), token);
        // TODO: Send email (Phase 4 requirement: just log it)
        log.info("SIMULATED EMAIL: To: {}, Subject: Verify your email, Link: http://localhost:8080/api/auth/verify-email?token={}", user.getEmail(), token);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Verification token expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);
        log.info("Email verified for user: {}", user.getEmail());
        eventProducer.sendAuthEvent(user, "EMAIL_VERIFIED", java.util.Collections.emptyMap());
    }
}
