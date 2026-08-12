package com.visaflow.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    // Company info for first user — subsequent users are invited via InvitationService
    private String companyName;
}
