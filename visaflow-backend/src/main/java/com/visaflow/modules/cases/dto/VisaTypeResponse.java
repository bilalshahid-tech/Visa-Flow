package com.visaflow.modules.cases.dto;

import com.visaflow.modules.cases.entity.DocumentRequirement;
import com.visaflow.modules.cases.entity.VisaType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class VisaTypeResponse {
    private UUID id;
    private String code;
    private String name;
    private String country;
    private List<RequirementResponse> requirements;

    @Data
    @Builder
    public static class RequirementResponse {
        private UUID id;
        private String documentClass;
        private String label;
        private boolean mandatory;
        private int displayOrder;

        public static RequirementResponse fromEntity(DocumentRequirement req) {
            return RequirementResponse.builder()
                    .id(req.getId())
                    .documentClass(req.getDocumentClass())
                    .label(req.getLabel())
                    .mandatory(req.isMandatory())
                    .displayOrder(req.getDisplayOrder())
                    .build();
        }
    }

    public static VisaTypeResponse fromEntity(VisaType vt, List<DocumentRequirement> reqs) {
        return VisaTypeResponse.builder()
                .id(vt.getId())
                .code(vt.getCode())
                .name(vt.getName())
                .country(vt.getCountry())
                .requirements(reqs != null ? reqs.stream().map(RequirementResponse::fromEntity).toList() : List.of())
                .build();
    }
}
