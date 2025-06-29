package com.listify.backend.controller;

import com.listify.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller for handling file storage operations.
 * <p>
 * This class exposes an endpoint under the {@code /api/storage} path for uploading files,
 * such as user avatars, to an external cloud storage provider.
 */
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);
    private final StorageService storageService;

    /**
     * Handles the upload of a single file.
     * <p>
     * This endpoint accepts a multipart file, delegates the upload process to the
     * {@link StorageService}, and returns the public URL of the stored file upon success.
     * The response is a JSON object containing a 'url' key, as expected by the frontend client.
     *
     * @param file The {@link MultipartFile} to be uploaded, received from the request part named 'file'.
     * @return a {@link ResponseEntity} containing a map with the public file URL,
     *         or a 500 Internal Server Error response if the upload fails.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("📥 Received file upload request for: {}", file.getOriginalFilename());
        try {
            String fileUrl = storageService.uploadFile(file);
            logger.info(" File successfully uploaded to Supabase. URL: {}", fileUrl);
            // The frontend client expects a JSON response with a "url" field.
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (Exception e) {
            logger.error(" Error during file upload in StorageController!", e);
            return ResponseEntity.status(500).body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }
}