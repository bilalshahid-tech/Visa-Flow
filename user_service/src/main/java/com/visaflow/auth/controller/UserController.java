package com.visaflow.auth.controller;

import com.visaflow.auth.dto.UserDTO;
import com.visaflow.auth.model.User;
import com.visaflow.auth.service.AuthenticationService;
import com.visaflow.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authenticationService.convertToDTO(user));
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserDTO updateRequest
    ) {
        return ResponseEntity.ok(userService.updateProfile(user, updateRequest));
    }
}
