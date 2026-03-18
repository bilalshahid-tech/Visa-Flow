package com.visaflow.user.service;

import com.visaflow.user.dto.*;
import com.visaflow.user.model.*;
import com.visaflow.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AuthServiceClient authServiceClient;
    private final DtoMapper dtoMapper;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        Map<String, Object> authDetails = authServiceClient.fetchUserDetails(userId);
        return dtoMapper.toProfileResponse(profile, authDetails);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest req) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));

        if (req.position() != null) profile.setPosition(req.position());
        if (req.department() != null) profile.setDepartment(req.department());
        if (req.phoneNumber() != null) profile.setPhoneNumber(req.phoneNumber());
        if (req.avatarUrl() != null) profile.setAvatarUrl(req.avatarUrl());
        if (req.language() != null) profile.setLanguage(req.language());
        if (req.timezone() != null) profile.setTimezone(req.timezone());
        if (req.metadata() != null) profile.setMetadata(req.metadata());

        profile = profileRepository.save(profile);
        kafkaEventProducer.publishProfileUpdated(profile.getId());

        Map<String, Object> authDetails = authServiceClient.fetchUserDetails(userId);
        return dtoMapper.toProfileResponse(profile, authDetails);
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> batchGetProfiles(List<UUID> userIds) {
        return profileRepository.findByUserIdIn(userIds).stream().map(p -> {
            Map<String, Object> authDetails = authServiceClient.fetchUserDetails(p.getUserId());
            return dtoMapper.toProfileResponse(p, authDetails);
        }).collect(Collectors.toList());
    }
}
