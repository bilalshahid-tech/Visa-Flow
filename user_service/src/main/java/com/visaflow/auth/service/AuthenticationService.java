package com.visaflow.auth.service;

import com.visaflow.auth.dto.*;
import com.visaflow.auth.model.Company;
import com.visaflow.auth.model.LoginHistory;
import com.visaflow.auth.model.Role;
import com.visaflow.auth.model.User;
import com.visaflow.auth.repository.CompanyRepository;
import com.visaflow.auth.repository.LoginHistoryRepository;
import com.visaflow.auth.repository.UserRepository;
import com.visaflow.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final com.visaflow.auth.kafka.AuthEventProducer eventProducer;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Company company = null;
        if (request.getRole() == Role.CONSULTANT && request.getCompanyName() != null) {
            company = Company.builder()
                    .name(request.getCompanyName())
                    .build();
            company = companyRepository.save(company);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : Role.CLIENT)
                .companyId(company != null ? company.getId() : null)
                .enabled(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        
        log.info("Registered new user: {}", user.getEmail());
        eventProducer.sendAuthEvent(user, "USER_REGISTERED", Map.of("role", user.getRole().name()));
        
        emailVerificationService.createVerificationToken(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(convertToDTO(user))
                .expiresIn(86400) // 1 day in seconds
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLoginAt(java.time.Instant.now());
        userRepository.save(user);

        LoginHistory history = LoginHistory.builder()
                .user(user)
                .status("SUCCESS")
                .build();
        loginHistoryRepository.save(history);

        log.info("User logged in: {}", user.getEmail());
        eventProducer.sendAuthEvent(user, "USER_LOGGED_IN", Collections.emptyMap());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(convertToDTO(user))
                .expiresIn(86400)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(com.visaflow.auth.model.RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .user(convertToDTO(user))
                            .expiresIn(86400)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(Collections.singletonList(user.getRole().name()))
                .companyId(user.getCompanyId())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
