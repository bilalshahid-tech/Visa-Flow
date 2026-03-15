package com.visaflow.auth.service;

import com.visaflow.auth.dto.InternalUserValidationRequest;
import com.visaflow.auth.dto.InternalUserValidationResponse;
import com.visaflow.auth.dto.UserDTO;
import com.visaflow.auth.model.User;
import com.visaflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class InternalAuthService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Value("${application.security.internal-api-key:secret-key-123}")
    private String internalApiKey;

    public InternalUserValidationResponse validateUser(InternalUserValidationRequest request, String apiKey) {
        if (apiKey == null || !java.security.MessageDigest.isEqual(internalApiKey.getBytes(), apiKey.getBytes())) {
            return InternalUserValidationResponse.builder().valid(false).build();
        }

        return userRepository.findById(request.getUserId())
                .map(user -> {
                    boolean hasRoles = request.getRequiredRoles() == null || 
                            request.getRequiredRoles().stream().allMatch(role -> user.getRole().name().equals(role));
                    
                    return InternalUserValidationResponse.builder()
                            .valid(true)
                            .user(authenticationService.convertToDTO(user))
                            .hasRequiredRoles(hasRoles)
                            .hasRequiredPermissions(true) // Permissions logic could be added here
                            .build();
                })
                .orElse(InternalUserValidationResponse.builder().valid(false).build());
    }

    public UserDTO getUserById(java.util.UUID userId, String apiKey) {
        if (apiKey == null || !java.security.MessageDigest.isEqual(internalApiKey.getBytes(), apiKey.getBytes())) {
            throw new RuntimeException("Invalid API Key");
        }
        return userRepository.findById(userId)
                .map(authenticationService::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
