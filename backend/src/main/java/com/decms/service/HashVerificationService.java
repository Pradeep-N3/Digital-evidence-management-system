package com.decms.service;

import com.decms.domain.model.custody.CustodyAction;
import com.decms.domain.model.custody.IntegrityLog;
import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.evidence.VerificationStatus;
import com.decms.domain.model.user.User;
import com.decms.dto.EvidenceDTO;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.IntegrityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class HashVerificationService {

    private final StorageService storageService;
    private final EvidenceRepository evidenceRepository;
    private final IntegrityLogRepository integrityLogRepository;
    private final CustodyLedgerService custodyLedgerService;
    private final AuditService auditService;

    public HashVerificationService(StorageService storageService,
                                   EvidenceRepository evidenceRepository,
                                   IntegrityLogRepository integrityLogRepository,
                                   CustodyLedgerService custodyLedgerService,
                                   AuditService auditService) {
        this.storageService = storageService;
        this.evidenceRepository = evidenceRepository;
        this.integrityLogRepository = integrityLogRepository;
        this.custodyLedgerService = custodyLedgerService;
        this.auditService = auditService;
    }

    @Transactional
    public EvidenceDTO.IntegrityCheckResponseDTO verifyEvidence(Evidence evidence, User verifier) {
        Path resolvedPath = storageService.resolveStoredFilePath(evidence.getStoredFilePath());
        if (!resolvedPath.toString().equals(evidence.getStoredFilePath())) {
            evidence.setStoredFilePath(resolvedPath.toString());
        }

        String physicalHash = storageService.computeFileSha256(resolvedPath);
        boolean isMatch = evidence.verifyIntegrity(physicalHash);

        evidenceRepository.save(evidence);

        // Record Integrity Log
        String remarks = isMatch ? "Automated byte-level SHA-256 verification passed: file matches registered baseline."
                                 : "ALERT: Cryptographic SHA-256 hash mismatch! Evidence may have been tampered or corrupted.";
        IntegrityLog log = new IntegrityLog(
                evidence,
                verifier,
                evidence.getOriginalSha256(),
                physicalHash,
                evidence.getStatus(),
                remarks
        );
        integrityLogRepository.save(log);

        // Record Custody Ledger Event
        custodyLedgerService.recordCustodyEvent(
                evidence,
                verifier,
                CustodyAction.VERIFIED,
                null,
                String.format("Integrity verified: %s (Calculated Hash: %s)", evidence.getStatus(), physicalHash)
        );

        // Record Security Audit Log
        auditService.recordEvent(
                verifier,
                "INTEGRITY_VERIFICATION",
                "Evidence",
                evidence.getEvidenceId(),
                "127.0.0.1",
                isMatch ? "SUCCESS" : "FAILURE",
                String.format("Verification Result: %s. Baseline: %s, Current: %s",
                        evidence.getStatus(), evidence.getOriginalSha256(), physicalHash)
        );

        EvidenceDTO.IntegrityCheckResponseDTO response = new EvidenceDTO.IntegrityCheckResponseDTO();
        response.setEvidenceId(evidence.getEvidenceId());
        response.setFileName(evidence.getFileName());
        response.setOriginalHash(evidence.getOriginalSha256());
        response.setCalculatedHash(physicalHash);
        response.setStatus(evidence.getStatus());
        response.setMatch(isMatch);
        response.setVerifiedAt(LocalDateTime.now());
        response.setVerifiedByUsername(verifier != null ? verifier.getUsername() : "SYSTEM");

        return response;
    }
}
