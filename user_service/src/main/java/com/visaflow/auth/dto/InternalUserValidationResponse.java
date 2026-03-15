package com.visaflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserValidationResponse {
    private boolean valid;
    private UserDTO user;
    private boolean hasRequiredRoles;
    private boolean hasRequiredPermissions;
}
