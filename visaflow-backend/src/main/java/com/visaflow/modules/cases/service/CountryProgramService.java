package com.visaflow.modules.cases.service;

import com.visaflow.modules.cases.dto.VisaProgramDto;
import com.visaflow.modules.cases.entity.Country;
import com.visaflow.modules.cases.entity.VisaProgram;
import com.visaflow.modules.cases.repository.CountryRepository;
import com.visaflow.modules.cases.repository.VisaProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryProgramService {

    private final CountryRepository countryRepository;
    private final VisaProgramRepository visaProgramRepository;

    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<VisaProgramDto> getProgramsForCountry(UUID countryId) {
        List<VisaProgram> programs = visaProgramRepository.findByCountryIdAndActiveTrue(countryId);
        return programs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisaProgramDto> getAllActivePrograms() {
        List<VisaProgram> programs = visaProgramRepository.findAllActive();
        return programs.stream().map(this::toDto).collect(Collectors.toList());
    }

    private VisaProgramDto toDto(VisaProgram p) {
        boolean hasJobRoleCondition = p.getStages() != null && p.getStages().stream()
                .anyMatch(stage -> stage.getRequirements() != null && stage.getRequirements().stream()
                        .anyMatch(req -> "job_role_category".equalsIgnoreCase(req.getConditionalField())));

        return VisaProgramDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription() != null ? p.getDescription() : "")
                .countryId(p.getCountry().getId())
                .countryName(p.getCountry().getName())
                .categoryName(p.getVisaCategory().getName())
                .hasJobRoleCondition(hasJobRoleCondition)
                .build();
    }
}
