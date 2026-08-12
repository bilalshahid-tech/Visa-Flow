package com.visaflow.modules.auth.service;

import com.visaflow.modules.auth.entity.Company;
import com.visaflow.modules.auth.entity.RefreshToken;
import com.visaflow.modules.auth.entity.User;
import com.visaflow.modules.auth.entity.enums.Role;
import com.visaflow.modules.auth.dto.AuthResponse;
import com.visaflow.modules.auth.dto.LoginRequest;
import com.visaflow.modules.auth.dto.RefreshTokenRequest;
import com.visaflow.modules.auth.dto.RegisterRequest;
import com.visaflow.modules.auth.repository.CompanyRepository;
import com.visaflow.modules.auth.repository.RefreshTokenRepository;
import com.visaflow.modules.auth.repository.UserRepository;
import com.visaflow.modules.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // Create company for the first admin user
        Company company = Company.builder()
                .name(request.getCompanyName() != null ? request.getCompanyName() : request.getEmail())
                .subscriptionPlan("TRIAL")
                .subscriptionStatus("ACTIVE")
                .maxUsers(5)
                .maxCases(50)
                .build();
        company = companyRepository.save(company);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .companyId(company.getId())
                .role(Role.ADMIN)
                .enabled(true)
                .emailVerified(true)
                .build();
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found or expired"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "user_id", user.getId().toString(),
                "company_id", user.getCompanyId() != null ? user.getCompanyId().toString() : "",
                "role", user.getRole().name()
        );
        String accessToken = jwtService.generateToken(claims, user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyId(user.getCompanyId() != null ? user.getCompanyId().toString() : null)
                .userId(user.getId().toString())
                .build();
    }
}
