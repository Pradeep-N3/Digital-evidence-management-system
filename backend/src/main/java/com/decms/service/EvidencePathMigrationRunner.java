package com.decms.service;

import com.decms.domain.model.evidence.Evidence;
import com.decms.repository.EvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

/**
 * Automatically inspects existing evidence entities on system startup and heals any
 * stored physical file paths that were invalidated by directory relocations or machine moves.
 */
@Component
@Order(10)
public class EvidencePathMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EvidencePathMigrationRunner.class);

    private final EvidenceRepository evidenceRepository;
    private final StorageService storageService;

    public EvidencePathMigrationRunner(EvidenceRepository evidenceRepository, StorageService storageService) {
        this.evidenceRepository = evidenceRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            List<Evidence> allEvidence = evidenceRepository.findAll();
            int updatedCount = 0;
            for (Evidence evidence : allEvidence) {
                String oldPath = evidence.getStoredFilePath();
                try {
                    Path resolved = storageService.resolveStoredFilePath(oldPath);
                    if (!resolved.toString().equals(oldPath)) {
                        log.info("Migrated evidence [{}] file path from [{}] to current location [{}]",
                                evidence.getEvidenceId(), oldPath, resolved);
                        evidence.setStoredFilePath(resolved.toString());
                        evidenceRepository.save(evidence);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    log.warn("Could not auto-resolve evidence [{}] at [{}]: {}", evidence.getEvidenceId(), oldPath, e.getMessage());
                }
            }
            if (updatedCount > 0) {
                log.info("DECMS Path Migration: successfully updated {} evidence file path(s) to current directory.", updatedCount);
            }
        } catch (Exception e) {
            log.warn("Evidence location migration skipped: {}", e.getMessage());
        }
    }
}

