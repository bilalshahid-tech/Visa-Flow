package com.visaflow.modules.cases.service;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.dto.ClientResponse;
import com.visaflow.modules.cases.dto.CreateClientRequest;
import com.visaflow.modules.cases.entity.Client;
import com.visaflow.modules.cases.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

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
}
