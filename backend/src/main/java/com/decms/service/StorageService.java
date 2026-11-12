package com.decms.service;

import com.decms.exception.BadRequestException;
import com.decms.exception.ResourceNotFoundException;
import com.decms.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class StorageService {

    private final Path rootLocation;

    public StorageService(@Value("${decms.storage.upload-dir:./vault}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public Path getCaseVault(String caseId) {
        Path casePath = this.rootLocation.resolve("cases").resolve(caseId).normalize();
        try {
            Files.createDirectories(casePath);
            return casePath;
        } catch (IOException e) {
            throw new RuntimeException("Could not create case vault directory: " + caseId, e);
        }
    }

    public String storeFile(MultipartFile file, String caseId, String evidenceId) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid filename contains relative path sequence: " + originalFilename);
        }

        try {
            Path targetFolder = getCaseVault(caseId);
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String storedFileName = evidenceId + "_" + System.currentTimeMillis() + extension;
            Path destinationFile = targetFolder.resolve(storedFileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in vault", e);
        }
    }

    public String computeFileSha256(String storedFilePath) {
        Path path = Paths.get(storedFilePath);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Evidence physical file not found at: " + storedFilePath);
        }
        try (InputStream is = Files.newInputStream(path)) {
            return CryptoUtil.sha256Stream(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for hash computation", e);
        }
    }

    public Resource loadFileAsResource(String storedFilePath) {
        try {
            Path filePath = Paths.get(storedFilePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or unreadable: " + storedFilePath);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path is invalid: " + storedFilePath);
        }
    }
}
