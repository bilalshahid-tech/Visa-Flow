package com.visaflow.auth.service;

import com.visaflow.auth.dto.AuthResponse;
import com.visaflow.auth.dto.LoginRequest;
import com.visaflow.auth.dto.RegisterRequest;
import com.visaflow.auth.model.Role;
import com.visaflow.auth.model.User;
import com.visaflow.auth.repository.CompanyRepository;
import com.visaflow.auth.repository.LoginHistoryRepository;
import com.visaflow.auth.repository.UserRepository;
import com.visaflow.auth.security.JwtService;
import com.visaflow.auth.kafka.AuthEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuthEventProducer eventProducer;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CLIENT)
                .build();
    }

    @Test
    void register_ShouldCreateUserAndReturnResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(
                com.visaflow.auth.model.RefreshToken.builder().token("refreshToken").build()
        );

        AuthResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        verify(userRepository).save(any(User.class));
        verify(eventProducer).sendAuthEvent(any(), eq("USER_REGISTERED"), any());
        verify(emailVerificationService).createVerificationToken(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authenticationService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateAndReturnResponse() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "Password123!", false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(
                com.visaflow.auth.model.RefreshToken.builder().token("refreshToken").build()
        );

        AuthResponse response = authenticationService.login(loginRequest);

        assertNotNull(response);
        verify(authenticationManager).authenticate(any());
        verify(loginHistoryRepository).save(any());
    }
}
