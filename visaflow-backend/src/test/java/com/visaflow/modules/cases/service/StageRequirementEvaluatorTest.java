package com.visaflow.modules.cases.service;

import com.visaflow.modules.cases.dto.CaseDetailResponse;
import com.visaflow.modules.cases.entity.StageDocumentRequirement;
import com.visaflow.modules.cases.entity.VisaCase;
import com.visaflow.modules.cases.entity.VisaProgramStage;
import com.visaflow.modules.cases.repository.*;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageRequirementEvaluatorTest {

    @Mock private VisaCaseRepository caseRepository;
    @Mock private VisaProgramStageRepository stageRepository;
    @Mock private StageDocumentRequirementRepository requirementRepository;
    @Mock private CaseStageHistoryRepository stageHistoryRepository;
    @Mock private DocumentRepository documentRepository;

    @InjectMocks
    private ProgramStageService programStageService;

    private VisaCase tourismCase;
    private VisaCase servicesCase;
    private UUID stageId;
    private StageDocumentRequirement unconditionalReq;
    private StageDocumentRequirement conditionalReq;

    @BeforeEach
    void setUp() {
        stageId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        tourismCase = VisaCase.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .jobRoleCategory("TOURISM")
                .build();

        servicesCase = VisaCase.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .jobRoleCategory("SERVICES")
                .build();

        unconditionalReq = StageDocumentRequirement.builder()
                .id(UUID.randomUUID())
                .documentType("TRAVEL_INSURANCE")
                .displayName("Travel Insurance")
                .mandatory(true)
                .displayOrder(1)
                .build();

        conditionalReq = StageDocumentRequirement.builder()
                .id(UUID.randomUUID())
                .documentType("SKILLS_PASS_PROOF")
                .displayName("Skills Pass Proof (Tourism roles)")
                .mandatory(false)
                .conditionalField("job_role_category")
                .conditionalOperator("EQUALS")
                .conditionalValue("TOURISM")
                .notes("Required for Tourism job role category cases only.")
                .displayOrder(2)
                .build();
    }

    @Test
    void tourismCase_includesConditionalRequirementAsMandatory() {
        when(requirementRepository.findByStageIdOrderByDisplayOrderAsc(stageId))
                .thenReturn(List.of(unconditionalReq, conditionalReq));
        when(documentRepository.findByCaseIdAndCompanyId(eq(tourismCase.getId()), eq(tourismCase.getCompanyId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<CaseDetailResponse.StageChecklistItemResponse> checklist =
                programStageService.buildStageChecklistInternal(tourismCase, stageId);

        assertThat(checklist).hasSize(2);
        
        var conditionalItem = checklist.stream()
                .filter(i -> i.getRequirementId().equals(conditionalReq.getId()))
                .findFirst().orElseThrow();

        assertThat(conditionalItem.isConditionallyMandatory()).isTrue();
        assertThat(conditionalItem.getDisplayName()).contains("Skills Pass Proof");
    }

    @Test
    void servicesCase_excludesConditionalRequirement() {
        when(requirementRepository.findByStageIdOrderByDisplayOrderAsc(stageId))
                .thenReturn(List.of(unconditionalReq, conditionalReq));
        when(documentRepository.findByCaseIdAndCompanyId(eq(servicesCase.getId()), eq(servicesCase.getCompanyId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<CaseDetailResponse.StageChecklistItemResponse> checklist =
                programStageService.buildStageChecklistInternal(servicesCase, stageId);

        // Conditional requirement does not match "SERVICES", so it is hidden
        assertThat(checklist).hasSize(1);
        assertThat(checklist.get(0).getRequirementId()).isEqualTo(unconditionalReq.getId());
    }
}
