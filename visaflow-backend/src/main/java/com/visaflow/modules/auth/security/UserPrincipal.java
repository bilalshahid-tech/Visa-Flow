package com.visaflow.modules.auth.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final UUID userId;
    private final UUID companyId;
    private final String email;
    private final String role;
}
