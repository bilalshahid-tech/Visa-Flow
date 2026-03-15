package com.visaflow.auth.service;

import com.visaflow.auth.model.PasswordResetToken;
import com.visaflow.auth.model.User;
import com.visaflow.auth.repository.PasswordResetTokenRepository;
import com.visaflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.visaflow.auth.kafka.AuthEventProducer eventProducer;

    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(3600)) // 1 hour
                .build();
        tokenRepository.save(resetToken);

        log.info("Created password reset token for user: {}. Token: {}", email, token);
        // TODO: Send email
        log.info("SIMULATED EMAIL: To: {}, Subject: Reset your password, Link: http://localhost:8080/api/auth/reset-password?token={}", email, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Reset token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
        log.info("Password reset for user: {}", user.getEmail());
        eventProducer.sendAuthEvent(user, "PASSWORD_CHANGED", java.util.Collections.emptyMap());
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
        eventProducer.sendAuthEvent(user, "PASSWORD_CHANGED", java.util.Collections.emptyMap());
    }
}
