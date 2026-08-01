package com.finance.dashboard.service;

import com.finance.dashboard.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf"
    );

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb:5}")
    private long maxSizeMb;

    public String storeReceipt(MultipartFile file, Long userId) {
        if (file.isEmpty())
            throw new BadRequestException("File is empty");

        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new BadRequestException("Only JPG, PNG, WebP and PDF files are allowed");

        if (file.getSize() > maxSizeMb * 1024 * 1024)
            throw new BadRequestException("File size exceeds " + maxSizeMb + "MB limit");

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx > 0) extension = originalFilename.substring(dotIdx);

        String filename = "receipt_" + userId + "_" + UUID.randomUUID().toString().replace("-","") + extension;

        try {
            Path dir = Paths.get(uploadDir, "receipts", String.valueOf(userId));
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String stored = "receipts/" + userId + "/" + filename;
            log.info("Receipt stored: {}", stored);
            return stored;
        } catch (IOException e) {
            log.error("Failed to store receipt: {}", e.getMessage());
            throw new BadRequestException("Failed to store file. Please try again.");
        }
    }

    public byte[] loadReceipt(String path) {
        try {
            Path file = Paths.get(uploadDir, path);
            if (!Files.exists(file)) throw new BadRequestException("File not found");
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file");
        }
    }

    public void deleteReceipt(String path) {
        if (path == null || path.isBlank()) return;
        try {
            Path file = Paths.get(uploadDir, path);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete receipt: {}", e.getMessage());
        }
    }
}