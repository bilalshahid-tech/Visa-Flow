package com.visaflow.user.service;

import com.visaflow.user.dto.*;
import com.visaflow.user.model.*;
import com.visaflow.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final AuthServiceClient authServiceClient;
    private final ProfileRepository profileRepository;

    @Transactional
    public InvitationResponse inviteUser(UUID companyId, UUID invitedBy, InviteUserRequest req) {
        log.info("Creating invitation for email: {} to company: {}", req.email(), companyId);
        
        // Optional: Check if active invitation already exists
        invitationRepository.findByEmailAndCompanyIdAndStatus(req.email(), companyId, "PENDING")
                .ifPresent(inv -> {
                    throw new RuntimeException("An active invitation already exists for this email");
                });

        String token = UUID.randomUUID().toString();
        
        Invitation invitation = Invitation.builder()
                .companyId(companyId)
                .invitedBy(invitedBy)
                .email(req.email())
                .role(req.role() != null ? req.role() : "CONSULTANT")
                .position(req.position())
                .message(req.message())
                .status("PENDING")
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
                
        invitation = invitationRepository.save(invitation);
        
        // MOCK EMAIL SENDING
        log.info("MOCK EMAIL: Sending invitation to '{}', with token: {}", invitation.getEmail(), token);
        
        return new InvitationResponse(
            invitation.getId(), invitation.getEmail(), invitation.getRole(),
            invitation.getInvitedBy(), invitation.getCompanyId(), token,
            invitation.getExpiresAt(), invitation.getStatus()
        );
    }

    @Transactional
    public Map<String, String> acceptInvitation(String token) {
        log.info("Accepting invitation with token: {}", token);
        
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
                
        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation is no longer valid");
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new RuntimeException("Invitation has expired");
        }
        
        // Call Auth service to create user
        UUID newUserId = authServiceClient.createUser(
            invitation.getEmail(), 
            "Invited User", // Temporary name
            invitation.getRole(), 
            invitation.getCompanyId()
        );
        
        // Create Profile
        Profile profile = Profile.builder()
                .userId(newUserId)
                .companyId(invitation.getCompanyId())
                .position(invitation.getPosition())
                .language("en")
                .timezone("UTC")
                .build();
        profileRepository.save(profile);
        
        invitation.setStatus("ACCEPTED");
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        return Map.of(
            "message", "Invitation accepted successfully",
            "userId", newUserId.toString(),
            "companyId", invitation.getCompanyId().toString()
        );
    }
}
