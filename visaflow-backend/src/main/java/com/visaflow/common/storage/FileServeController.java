package com.visaflow.common.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * Serves locally-stored files when MinIO is not available (dev mode).
 *
 * URL pattern:  GET /api/files/companies/{companyId}/cases/{caseId}/{filename}
 * The full sub-path after /api/files/ is used as the storage key on disk.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileServeController {

    private final StorageService storageService;

    @GetMapping("/**")
    public ResponseEntity<byte[]> serveFile(HttpServletRequest request) throws IOException {
        // Extract everything after /api/files/
        String requestUri = request.getRequestURI();
        String prefix = "/api/files/";
        int idx = requestUri.indexOf(prefix);
        if (idx == -1) {
            return ResponseEntity.notFound().build();
        }
        String key = requestUri.substring(idx + prefix.length());

        if (storageService.isMinioAvailable()) {
            // Should not happen in production, but guard anyway
            return ResponseEntity.status(404).build();
        }

        try (InputStream in = storageService.openLocalFile(key)) {
            byte[] bytes = in.readAllBytes();
            String contentType = detectContentType(key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + extractFilename(key) + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes);
        } catch (java.io.FileNotFoundException e) {
            log.warn("File not found for key: {}", key);
            return ResponseEntity.notFound().build();
        }
    }

    private String extractFilename(String key) {
        int last = key.lastIndexOf('/');
        return last >= 0 ? key.substring(last + 1) : key;
    }

    private String detectContentType(String key) {
        if (key.endsWith(".pdf"))  return "application/pdf";
        if (key.endsWith(".jpg") || key.endsWith(".jpeg")) return "image/jpeg";
        if (key.endsWith(".png"))  return "image/png";
        if (key.endsWith(".gif"))  return "image/gif";
        if (key.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
