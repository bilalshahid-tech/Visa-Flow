package com.visaflow.user.controller;

import com.visaflow.user.dto.*;
import com.visaflow.user.service.InvitationService;
import com.visaflow.user.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationResponse inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return invitationService.inviteUser(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), request);
    }

    @PostMapping("/accept")
    public Map<String, String> acceptInvitation(@RequestParam String token) {
        return invitationService.acceptInvitation(token);
    }
}
