package com.visaflow.modules.cases.controller;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.dto.ClientDetailResponse;
import com.visaflow.modules.cases.dto.ClientResponse;
import com.visaflow.modules.cases.dto.CreateClientRequest;
import com.visaflow.modules.cases.dto.UpdateClientRequest;
import com.visaflow.modules.cases.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody CreateClientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(request, principal));
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clientService.searchClients(search, principal, pageable));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDetailResponse> getClientDetail(
            @PathVariable UUID clientId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clientService.getClientDetail(clientId, principal));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDetailResponse> updateClient(
            @PathVariable UUID clientId,
            @Valid @RequestBody UpdateClientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clientService.updateClient(clientId, request, principal));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable UUID clientId,
            @AuthenticationPrincipal UserPrincipal principal) {
        clientService.deleteClient(clientId, principal);
        return ResponseEntity.noContent().build();
    }
}
