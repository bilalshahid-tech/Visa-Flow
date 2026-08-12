package com.visaflow.modules.cases.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateClientRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Passport number is required")
    private String passportNumber;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String phone;

    @Email(message = "Must be a valid email address")
    private String email;

    private String address;
}
