package com.visaflow.cases.dto.request;

import com.visaflow.cases.domain.enums.VisaType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseRequest {
    @NotNull(message = "Applicant ID must not be null")
    private UUID applicantId;

    @NotNull(message = "Visa type must be specified")
    private VisaType visaType;
}
