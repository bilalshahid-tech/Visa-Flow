package com.visaflow.modules.cases.controller;

import com.visaflow.modules.cases.dto.VisaTypeResponse;
import com.visaflow.modules.cases.entity.DocumentRequirement;
import com.visaflow.modules.cases.entity.VisaType;
import com.visaflow.modules.cases.repository.DocumentRequirementRepository;
import com.visaflow.modules.cases.repository.VisaTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visa-types")
@RequiredArgsConstructor
public class VisaTypeController {

    private final VisaTypeRepository visaTypeRepository;
    private final DocumentRequirementRepository requirementRepository;

    @GetMapping
    public ResponseEntity<List<VisaTypeResponse>> listAll() {
        List<VisaType> types = visaTypeRepository.findAll();
        List<VisaTypeResponse> response = types.stream()
                .map(vt -> VisaTypeResponse.fromEntity(vt, null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/requirements")
    public ResponseEntity<List<VisaTypeResponse.RequirementResponse>> getRequirements(@PathVariable UUID id) {
        if (!visaTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Visa type not found: " + id);
        }
        List<DocumentRequirement> reqs = requirementRepository.findByVisaTypeIdOrderByDisplayOrderAsc(id);
        List<VisaTypeResponse.RequirementResponse> response = reqs.stream()
                .map(VisaTypeResponse.RequirementResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
