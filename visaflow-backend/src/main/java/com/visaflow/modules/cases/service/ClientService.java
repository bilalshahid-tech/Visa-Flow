package com.visaflow.modules.cases.service;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.dto.ClientDetailResponse;
import com.visaflow.modules.cases.dto.ClientResponse;
import com.visaflow.modules.cases.dto.CreateClientRequest;
import com.visaflow.modules.cases.dto.UpdateClientRequest;
import com.visaflow.modules.cases.entity.Client;
import com.visaflow.modules.cases.entity.VisaCase;
import com.visaflow.modules.cases.repository.ClientRepository;
import com.visaflow.modules.cases.repository.VisaCaseRepository;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final VisaCaseRepository visaCaseRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public ClientResponse createClient(CreateClientRequest request, UserPrincipal principal) {
        Client client = Client.builder()
                .companyId(principal.getCompanyId())
                .fullName(request.getFullName())
                .passportNumber(request.getPassportNumber())
                .nationality(request.getNationality())
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();

        client = clientRepository.save(client);
        log.info("Client created: id={} company={}", client.getId(), principal.getCompanyId());
        return toResponse(client);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(String query, UserPrincipal principal, Pageable pageable) {
        return clientRepository.searchByCompany(principal.getCompanyId(), query == null ? "" : query, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClient(UUID clientId, UserPrincipal principal) {
        Client client = clientRepository.findByIdAndCompanyId(clientId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
        return toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientDetailResponse getClientDetail(UUID clientId, UserPrincipal principal) {
        log.info("Fetching client detail: clientId={}, companyId={}", clientId, principal != null ? principal.getCompanyId() : null);
        try {
            Client client = clientRepository.findByIdAndCompanyId(clientId, principal.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + clientId));

            List<VisaCase> cases = visaCaseRepository.findByClientIdAndCompanyIdOrderByCreatedAtDesc(clientId, principal.getCompanyId());

            List<ClientDetailResponse.ClientCaseSummaryResponse> caseSummaries = cases.stream()
                    .map(c -> {
                        String progName = null;
                        try {
                            if (c.getVisaProgram() != null) progName = c.getVisaProgram().getName();
                        } catch (Exception e) {
                            log.warn("Could not load visaProgram for case {}: {}", c.getId(), e.getMessage());
                        }

                        String typeName = null;
                        try {
                            if (c.getVisaType() != null) typeName = c.getVisaType().getName();
                        } catch (Exception e) {
                            log.warn("Could not load visaType for case {}: {}", c.getId(), e.getMessage());
                        }

                        String stageName = null;
                        try {
                            if (c.getCurrentStage() != null) stageName = c.getCurrentStage().getName();
                        } catch (Exception e) {
                            log.warn("Could not load currentStage for case {}: {}", c.getId(), e.getMessage());
                        }

                        return ClientDetailResponse.ClientCaseSummaryResponse.builder()
                                .id(c.getId())
                                .caseReference(c.getCaseReference())
                                .visaProgramName(progName)
                                .visaTypeName(typeName)
                                .status(c.getStatus() != null ? friendlyStatus(c.getStatus().name()) : "Draft")
                                .currentStageName(stageName)
                                .createdAt(c.getCreatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

            return toClientDetailResponse(client, caseSummaries);
        } catch (Exception e) {
            log.error("Error fetching client detail for clientId={}: {}", clientId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ClientDetailResponse updateClient(UUID clientId, UpdateClientRequest request, UserPrincipal principal) {
        Client client = clientRepository.findByIdAndCompanyId(clientId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));

        // Check passport uniqueness if passport number changed
        if (!client.getPassportNumber().equalsIgnoreCase(request.getPassportNumber())) {
            boolean exists = clientRepository.existsByCompanyIdAndPassportNumber(principal.getCompanyId(), request.getPassportNumber());
            if (exists) {
                throw new IllegalArgumentException("A client with passport number " + request.getPassportNumber() + " already exists in your consultancy.");
            }
        }

        client.setFullName(request.getFullName());
        client.setPassportNumber(request.getPassportNumber());
        client.setNationality(request.getNationality());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());

        client = clientRepository.save(client);
        log.info("Client updated: id={} company={}", client.getId(), principal.getCompanyId());

        return getClientDetail(clientId, principal);
    }

    @Transactional
    public void deleteClient(UUID clientId, UserPrincipal principal) {
        Client client = clientRepository.findByIdAndCompanyId(clientId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));

        List<VisaCase> cases = visaCaseRepository.findByClientIdAndCompanyIdOrderByCreatedAtDesc(clientId, principal.getCompanyId());
        if (!cases.isEmpty()) {
            List<UUID> caseIds = cases.stream().map(VisaCase::getId).collect(Collectors.toList());
            List<Document> documents = documentRepository.findByCaseIdIn(caseIds);
            if (!documents.isEmpty()) {
                documentRepository.deleteAll(documents);
            }
            visaCaseRepository.deleteAll(cases);
        }

        clientRepository.delete(client);
        log.info("Client deleted: id={} company={}", clientId, principal.getCompanyId());
    }

    public void assertClientBelongsToCompany(UUID clientId, UUID companyId) {
        if (!clientRepository.findByIdAndCompanyId(clientId, companyId).isPresent()) {
            throw new AccessDeniedException("Client does not belong to your consultancy");
        }
    }

    private ClientResponse toResponse(Client c) {
        return ClientResponse.builder()
                .id(c.getId())
                .companyId(c.getCompanyId())
                .fullName(c.getFullName())
                .passportNumber(c.getPassportNumber())
                .nationality(c.getNationality())
                .dateOfBirth(c.getDateOfBirth())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ClientDetailResponse toClientDetailResponse(Client c, List<ClientDetailResponse.ClientCaseSummaryResponse> cases) {
        return ClientDetailResponse.builder()
                .id(c.getId())
                .companyId(c.getCompanyId())
                .fullName(c.getFullName())
                .passportNumber(c.getPassportNumber())
                .nationality(c.getNationality())
                .dateOfBirth(c.getDateOfBirth())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .cases(cases)
                .build();
    }

    private String friendlyStatus(String rawStatus) {
        return switch (rawStatus) {
            case "DRAFT"        -> "Draft";
            case "DOCS_PENDING" -> "Documents Pending";
            case "UNDER_REVIEW" -> "Under Review";
            case "SUBMITTED"    -> "Submitted";
            case "APPROVED"     -> "Approved";
            case "REJECTED"     -> "Rejected";
            case "CLOSED"       -> "Closed";
            default             -> rawStatus;
        };
    }
}
