package com.visaflow.common.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * Dual-mode storage service.
 *
 * • When MinIO is reachable (production / Docker): stores files in MinIO and
 *   returns pre-signed URLs.
 * • When MinIO is NOT reachable (local dev): falls back to local disk under
 *   FILE_UPLOAD_DIR and returns a direct /api/files/{key} URL.
 *
 * The mode is chosen once at startup (eager probe in constructor). No config
 * change is needed — it auto-detects based on reachability.
 */
@Slf4j
@Service
public class StorageService {

    private final String bucket;
    private final String uploadDir;
    private final String appBaseUrl;

    private MinioClient minioClient;
    private MinioClient presignMinioClient;
    private boolean minioAvailable = false;

    public StorageService(
            @Value("${application.storage.endpoint:http://minio:9000}")     String endpoint,
            @Value("${application.storage.external-url:http://localhost:9000}") String externalUrl,
            @Value("${application.storage.access-key:visaflow}")            String accessKey,
            @Value("${application.storage.secret-key:visaflow123}")          String secretKey,
            @Value("${application.storage.bucket:visaflow-documents}")       String bucket,
            @Value("${application.file.upload-dir:./uploads}")               String uploadDir,
            @Value("${application.base-url:http://localhost:8080}")          String appBaseUrl) {

        this.bucket    = bucket;
        this.uploadDir = uploadDir;
        this.appBaseUrl = appBaseUrl;

        // Probe MinIO availability once at startup
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1")
                    .build();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
            this.minioClient   = client;

            // Build separate MinioClient for browser presigning to avoid SignatureDoesNotMatch errors in host browser
            this.presignMinioClient = MinioClient.builder()
                    .endpoint(externalUrl)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1")
                    .build();

            this.minioAvailable = true;
            log.info("Storage mode: MinIO (endpoint={}, externalUrl={})", endpoint, externalUrl);
        } catch (Exception e) {
            log.warn("MinIO not reachable ({}). Falling back to local disk storage at '{}'.", e.getMessage(), uploadDir);
            this.minioAvailable = false;
            ensureUploadDir();
        }
    }

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    /**
     * Uploads a file and returns the storage key.
     * In MinIO mode: stores in the bucket.
     * In local mode:  stores under uploadDir, using the key as a relative path.
     */
    public String upload(String key, InputStream data, long size, String mimeType) {
        if (minioAvailable) {
            return uploadToMinio(key, data, size, mimeType);
        } else {
            return uploadToLocalDisk(key, data);
        }
    }

    private String uploadToMinio(String key, InputStream data, long size, String mimeType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(data, size, -1)
                    .contentType(mimeType)
                    .build());
            log.info("Uploaded to MinIO: key={} size={}", key, size);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    private String uploadToLocalDisk(String key, InputStream data) {
        try {
            Path target = Paths.get(uploadDir, key);
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Uploaded to local disk: path={}", target);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to local disk: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Pre-signed / view URL
    // -------------------------------------------------------------------------

    /**
     * Returns a URL to view the file.
     * In MinIO mode: 5-minute pre-signed URL.
     * In local mode:  a direct URL pointing to the file-serve endpoint.
     */
    public String generatePresignedUrl(String key, int expiryMinutes) {
        if (minioAvailable) {
            return generateMinioPresignedUrl(key, expiryMinutes);
        } else {
            // Return a URL our FileServeController can serve
            return appBaseUrl + "/api/files/" + key;
        }
    }

    private String generateMinioPresignedUrl(String key, int expiryMinutes) {
        try {
            return presignMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Local file access (used by FileServeController)
    // -------------------------------------------------------------------------

    public InputStream openLocalFile(String key) throws IOException {
        Path file = Paths.get(uploadDir, key);
        if (!Files.exists(file)) {
            throw new java.io.FileNotFoundException("File not found: " + key);
        }
        return new FileInputStream(file.toFile());
    }

    public String getLocalFilePath(String key) {
        return Paths.get(uploadDir, key).toString();
    }

    public boolean isMinioAvailable() {
        return minioAvailable;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void ensureUploadDir() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.error("Cannot create upload directory '{}': {}", uploadDir, e.getMessage());
        }
    }
}
