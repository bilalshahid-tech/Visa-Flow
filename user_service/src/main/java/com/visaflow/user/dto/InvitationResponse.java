package com.visaflow.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResponse(
    UUID id,
    String email,
    String role,
    Object invitedBy,
    UUID companyId,
    String token,
    LocalDateTime expiresAt,
    String status
) {}
