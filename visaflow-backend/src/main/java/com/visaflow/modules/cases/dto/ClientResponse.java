package com.visaflow.modules.cases.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ClientResponse {
    private UUID id;
    private UUID companyId;
    private String fullName;
    private String passportNumber;
    private String nationality;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;
}
