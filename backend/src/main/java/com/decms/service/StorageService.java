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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

    public Path getRootLocation() {
        return rootLocation;
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

    /**
     * Resolves the physical path of an evidence artifact, dynamically accommodating
     * project relocation, working-directory variations, and moved directories.
     */
    public Path resolveStoredFilePath(String storedFilePath) {
        if (storedFilePath == null || storedFilePath.trim().isEmpty()) {
            throw new ResourceNotFoundException("Evidence physical file path is empty");
        }

        // 1. Check exact path as-is
        try {
            Path directPath = Paths.get(storedFilePath).normalize();
            if (Files.exists(directPath) && Files.isRegularFile(directPath)) {
                return directPath.toAbsolutePath().normalize();
            }
        } catch (InvalidPathException ignored) {}

        // 2. Check relative to rootLocation
        try {
            Path relativeToRoot = this.rootLocation.resolve(storedFilePath).normalize();
            if (Files.exists(relativeToRoot) && Files.isRegularFile(relativeToRoot)) {
                return relativeToRoot.toAbsolutePath().normalize();
            }
        } catch (Exception ignored) {}

        // 3. Extract subpath after "vault" (handles project relocation)
        String normalized = storedFilePath.replace('\\', '/');
        int vaultIdx = normalized.indexOf("/vault/");
        String subPath = null;
        if (vaultIdx != -1) {
            subPath = normalized.substring(vaultIdx + 7);
        } else if (normalized.startsWith("vault/")) {
            subPath = normalized.substring(6);
        }

        if (subPath != null && !subPath.trim().isEmpty()) {
            List<Path> candidateBases = new ArrayList<>();
            candidateBases.add(this.rootLocation);
            candidateBases.add(Paths.get("vault").toAbsolutePath().normalize());
            candidateBases.add(Paths.get("backend", "vault").toAbsolutePath().normalize());
            if (this.rootLocation.getParent() != null) {
                candidateBases.add(this.rootLocation.getParent().resolve("vault").normalize());
                candidateBases.add(this.rootLocation.getParent().resolve("backend/vault").normalize());
            }
            candidateBases.add(Paths.get("..", "vault").toAbsolutePath().normalize());
            candidateBases.add(Paths.get("..", "backend", "vault").toAbsolutePath().normalize());

            for (Path base : candidateBases) {
                try {
                    Path candidate = base.resolve(subPath).normalize();
                    if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                        return candidate.toAbsolutePath().normalize();
                    }
                } catch (Exception ignored) {}
            }
        }

        // 4. Filename-based lookup across known vault storage directories
        String fileName = null;
        try {
            Path p = Paths.get(storedFilePath);
            if (p.getFileName() != null) {
                fileName = p.getFileName().toString();
            }
        } catch (Exception e) {
            int lastSlash = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
            fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        }

        if (fileName != null && !fileName.trim().isEmpty()) {
            final String targetFileName = fileName;
            List<Path> searchRoots = new ArrayList<>();
            searchRoots.add(this.rootLocation);
            searchRoots.add(Paths.get("vault").toAbsolutePath().normalize());
            searchRoots.add(Paths.get("backend", "vault").toAbsolutePath().normalize());
            if (this.rootLocation.getParent() != null) {
                searchRoots.add(this.rootLocation.getParent().resolve("vault").normalize());
                searchRoots.add(this.rootLocation.getParent().resolve("backend/vault").normalize());
            }
            searchRoots.add(Paths.get("..", "vault").toAbsolutePath().normalize());
            searchRoots.add(Paths.get("..", "backend", "vault").toAbsolutePath().normalize());

            for (Path searchDir : searchRoots) {
                if (searchDir != null && Files.exists(searchDir) && Files.isDirectory(searchDir)) {
                    try (Stream<Path> stream = Files.walk(searchDir, 6)) {
                        var found = stream
                                .filter(Files::isRegularFile)
                                .filter(f -> f.getFileName().toString().equalsIgnoreCase(targetFileName))
                                .findFirst();
                        if (found.isPresent()) {
                            return found.get().toAbsolutePath().normalize();
                        }
                    } catch (IOException ignored) {}
                }
            }
        }

        throw new ResourceNotFoundException("Evidence physical file not found at: " + storedFilePath);
    }

    public String computeFileSha256(String storedFilePath) {
        Path path = resolveStoredFilePath(storedFilePath);
        return computeFileSha256(path);
    }

    public String computeFileSha256(Path path) {
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Evidence physical file not found at: " + path);
        }
        try (InputStream is = Files.newInputStream(path)) {
            return CryptoUtil.sha256Stream(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for hash computation: " + path, e);
        }
    }

    public Resource loadFileAsResource(String storedFilePath) {
        try {
            Path filePath = resolveStoredFilePath(storedFilePath);
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
