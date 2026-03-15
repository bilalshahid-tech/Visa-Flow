package com.visaflow.auth.controller;

import com.visaflow.auth.dto.InternalUserValidationRequest;
import com.visaflow.auth.dto.InternalUserValidationResponse;
import com.visaflow.auth.dto.UserDTO;
import com.visaflow.auth.service.InternalAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final InternalAuthService internalAuthService;

    @PostMapping("/validate")
    public ResponseEntity<InternalUserValidationResponse> validateUser(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody InternalUserValidationRequest request
    ) {
        return ResponseEntity.ok(internalAuthService.validateUser(request, apiKey));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(internalAuthService.getUserById(userId, apiKey));
    }
}
