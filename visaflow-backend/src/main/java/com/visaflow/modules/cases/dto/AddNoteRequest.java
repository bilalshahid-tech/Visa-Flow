package com.visaflow.modules.cases.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddNoteRequest {
    @NotBlank(message = "Note body cannot be empty")
    private String body;
}
