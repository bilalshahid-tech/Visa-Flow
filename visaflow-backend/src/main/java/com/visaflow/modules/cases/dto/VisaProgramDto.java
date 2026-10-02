package com.visaflow.modules.cases.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VisaProgramDto {
    private UUID id;
    private String name;
    private String description;
    private UUID countryId;
    private String countryName;
    private String categoryName;
    private boolean hasJobRoleCondition;
}
