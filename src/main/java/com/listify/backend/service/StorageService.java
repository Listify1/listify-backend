package com.listify.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Service for handling file uploads to a Supabase Storage bucket.
 * <p>
 * This service communicates with the Supabase Storage API directly via HTTP requests
 * using {@link RestTemplate}, without relying on an S3-compatible client.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Value("${supabase.url}")
    private String supabaseUrl; // z.B. https://<id>.supabase.co/storage/v1

    @Value("${supabase.secret-key}")
    private String supabaseServiceKey; // Der lange service_role JWT

    @Value("${supabase.bucket-name}")
    private String bucketName;

    // Wir brauchen keinen S3Client mehr, also entfernen wir ihn aus dem Konstruktor
    public StorageService() {
    }

    /**
     * Uploads a file to the configured Supabase bucket.
     *
     * @param file The {@link MultipartFile} to upload.
     * @return The public URL of the uploaded file.
     * @throws IOException if the upload fails or an error occurs during communication.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String fileName = UUID.randomUUID() + "-" + originalFilename.replaceAll("\\s+", "_");

        // Wir bauen die URL für den Upload manuell zusammen
        // Format: <supabase-url>/object/<bucket-name>/<file-name>
        String uploadUrl = supabaseUrl + "/object/" + bucketName + "/" + fileName;

        logger.info("================ UPLOAD-VERSUCH START (HTTP) ================");
        logger.info("Upload-URL: {}", uploadUrl);
        logger.info("Bucket-Name: {}", bucketName);
        logger.info("Datei-Name: {}", fileName);
        logger.info("============================================================");


        // HTTP-Header setzen
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseServiceKey);
        headers.setContentType(MediaType.valueOf(file.getContentType()));

        // Den Request-Body mit den Bytes der Datei erstellen
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        // Einen RestTemplate für die HTTP-Anfrage erstellen
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Die POST-Anfrage an Supabase senden
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Erfolg prüfen
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Datei erfolgreich via HTTP hochgeladen. Antwort: {}", response.getBody());
            } else {
                logger.error("Fehler beim HTTP-Upload. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new IOException("Fehler beim Upload: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Kritischer Fehler beim Senden der HTTP-Anfrage!", e);
            throw new IOException("Fehler bei der Kommunikation mit dem Storage-Service.", e);
        }

        // Die öffentliche URL für den Zugriff auf die Datei zusammenbauen
        String publicUrl = supabaseUrl + "/object/public/" + bucketName + "/" + fileName;
        logger.info("Generierte öffentliche URL: {}", publicUrl);
        return publicUrl;
    }
}