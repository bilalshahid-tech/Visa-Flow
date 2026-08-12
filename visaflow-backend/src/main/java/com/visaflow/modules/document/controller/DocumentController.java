package com.visaflow.modules.document.controller;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.document.dto.DocumentReviewRequest;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases/{caseId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /** Upload a document — optionally against a checklist requirement */
    @PostMapping
    public ResponseEntity<Document> upload(
            @PathVariable UUID caseId,
            @RequestParam(required = false) UUID requirementId,
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(caseId, requirementId, file, principal));
    }

    /** Returns a 5-minute pre-signed URL to view/preview the document */
    @GetMapping("/{documentId}/view")
    public ResponseEntity<String> view(
            @PathVariable UUID caseId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String url = documentService.generateViewUrl(caseId, documentId, principal);
        return ResponseEntity.ok(url);
    }

    /** Approve or reject a document */
    @PatchMapping("/{documentId}/review")
    public ResponseEntity<Document> review(
            @PathVariable UUID caseId,
            @PathVariable UUID documentId,
            @Valid @RequestBody DocumentReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(documentService.reviewDocument(caseId, documentId, request, principal));
    }
}
