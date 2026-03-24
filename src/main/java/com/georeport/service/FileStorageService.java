package com.georeport.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file storage operations.
 * Manages image uploads for issues.
 */
@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    private Path uploadPath;
    private List<String> allowedExtensionList;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        allowedExtensionList = Arrays.asList(allowedExtensions.split(","));

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    /**
     * Store a file and return the generated filename
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Validate file
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFilename.contains("..")) {
            throw new IOException("Invalid file path: " + originalFilename);
        }

        // Check file extension
        String extension = getFileExtension(originalFilename);
        if (!allowedExtensionList.contains(extension.toLowerCase())) {
            throw new IOException("File type not allowed: " + extension);
        }

        // Generate unique filename
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // Store file
        Path targetLocation = uploadPath.resolve(newFilename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return newFilename;
    }

    /**
     * Delete a file
     */
    public boolean deleteFile(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get file path
     */
    public Path getFilePath(String filename) {
        return uploadPath.resolve(filename).normalize();
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * Get upload path
     */
    public Path getUploadPath() {
        return uploadPath;
    }
}
