package com.visaflow.user.controller;

import com.visaflow.user.dto.*;
import com.visaflow.user.service.ProfileService;
import com.visaflow.user.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ProfileResponse getMyProfile() {
        return profileService.getProfile(SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/me")
    public ProfileResponse updateMyProfile(@RequestBody ProfileUpdateRequest request) {
        return profileService.updateProfile(SecurityUtils.getCurrentUserId(), request);
    }

    @GetMapping("/{userId}")
    public ProfileResponse getProfile(@PathVariable UUID userId) {
        return profileService.getProfile(userId);
    }
}
