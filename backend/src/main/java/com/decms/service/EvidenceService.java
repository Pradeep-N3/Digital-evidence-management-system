package com.decms.service;

import com.decms.domain.model.custody.CustodyAction;
import com.decms.domain.model.custody.CustodyRecord;
import com.decms.domain.model.evidence.*;
import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.user.User;
import com.decms.dto.CustodyDTO;
import com.decms.dto.EvidenceDTO;
import com.decms.exception.ResourceNotFoundException;
import com.decms.repository.EvidenceRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final CaseService caseService;
    private final StorageService storageService;
    private final CustodyLedgerService custodyLedgerService;
    private final AuditService auditService;

    public EvidenceService(EvidenceRepository evidenceRepository,
                           CaseService caseService,
                           StorageService storageService,
                           CustodyLedgerService custodyLedgerService,
                           AuditService auditService) {
        this.evidenceRepository = evidenceRepository;
        this.caseService = caseService;
        this.storageService = storageService;
        this.custodyLedgerService = custodyLedgerService;
        this.auditService = auditService;
    }

    private synchronized String generateNextEvidenceId() {
        int year = Year.now().getValue();
        long count = evidenceRepository.count() + 1;
        return String.format("EVD-%d-%04d", year, count);
    }

    @Transactional
    public EvidenceDTO.EvidenceSummaryDTO uploadEvidence(String caseId, MultipartFile file, User uploader) {
        Case targetCase = caseService.getCaseEntity(caseId);
        String evidenceId = generateNextEvidenceId();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed_artifact";

        // Store physical file
        String storedFilePath = storageService.storeFile(file, caseId, evidenceId);
        long sizeBytes = file.getSize();
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // Compute baseline SHA-256
        String baselineHash = storageService.computeFileSha256(storedFilePath);

        // Polymorphic evidence instantiation based on MIME type / extension
        Evidence evidence = createPolymorphicEvidence(
                evidenceId,
                originalFilename,
                storedFilePath,
                sizeBytes,
                mimeType,
                baselineHash,
                uploader,
                targetCase
        );

        Evidence savedEvidence = evidenceRepository.save(evidence);

        // Record Genesis Custody Record
        custodyLedgerService.recordCustodyEvent(
                savedEvidence,
                uploader,
                CustodyAction.UPLOADED,
                null,
                String.format("Initial evidence intake registered. Baseline SHA-256: %s", baselineHash)
        );

        // Audit Log
        auditService.recordEvent(
                uploader,
                "EVIDENCE_INGESTION",
                "Evidence",
                savedEvidence.getEvidenceId(),
                "127.0.0.1",
                "SUCCESS",
                String.format("File '%s' (%d bytes) ingested into case %s", originalFilename, sizeBytes, caseId)
        );

        return caseService.mapEvidenceToSummary(savedEvidence);
    }

    private Evidence createPolymorphicEvidence(String evidenceId, String filename, String filePath,
                                               long sizeBytes, String mimeType, String hash,
                                               User uploader, Case c) {
        String lower = filename.toLowerCase();
        if (mimeType.startsWith("image/") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp")) {
            return new ImageEvidence(evidenceId, filename, filePath, sizeBytes, mimeType, hash, uploader, c, 1920, 1080, "sRGB");
        } else if (mimeType.equals("application/pdf") || lower.endsWith(".pdf") || lower.endsWith(".docx") || lower.endsWith(".doc")) {
            return new DocumentEvidence(evidenceId, filename, filePath, sizeBytes, mimeType, hash, uploader, c, 1, "PDF Document");
        } else if (mimeType.startsWith("video/") || mimeType.startsWith("audio/") || lower.endsWith(".mp4") || lower.endsWith(".mp3") || lower.endsWith(".wav")) {
            return new MediaEvidence(evidenceId, filename, filePath, sizeBytes, mimeType, hash, uploader, c, 60L, "H.264/AAC");
        } else if (lower.endsWith(".log") || lower.endsWith(".txt") || lower.endsWith(".csv") || lower.endsWith(".pcap") || lower.endsWith(".json")) {
            return new LogEvidence(evidenceId, filename, filePath, sizeBytes, mimeType, hash, uploader, c, 250L, "System Log Artifact");
        } else {
            return new DocumentEvidence(evidenceId, filename, filePath, sizeBytes, mimeType, hash, uploader, c, 1, "Generic Artifact");
        }
    }

    public List<EvidenceDTO.EvidenceSummaryDTO> getAllEvidence() {
        List<Evidence> list = evidenceRepository.findAllByOrderByUploadedAtDesc();
        List<EvidenceDTO.EvidenceSummaryDTO> result = new ArrayList<>();
        for (Evidence e : list) {
            result.add(caseService.mapEvidenceToSummary(e));
        }
        return result;
    }

    public Evidence getEvidenceEntity(String evidenceId) {
        return evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with ID: " + evidenceId));
    }

    public EvidenceDTO.EvidenceSummaryDTO getEvidenceById(String evidenceId) {
        Evidence e = getEvidenceEntity(evidenceId);
        return caseService.mapEvidenceToSummary(e);
    }

    public Resource getEvidenceFileResource(String evidenceId, User accessor) {
        Evidence e = getEvidenceEntity(evidenceId);

        // Record Custody download
        custodyLedgerService.recordCustodyEvent(
                e,
                accessor,
                CustodyAction.DOWNLOADED,
                accessor.getFullName(),
                "File accessed/downloaded for forensic review"
        );

        auditService.recordEvent(
                accessor,
                "EVIDENCE_DOWNLOAD",
                "Evidence",
                e.getEvidenceId(),
                "127.0.0.1",
                "SUCCESS",
                String.format("Evidence file '%s' downloaded by %s", e.getFileName(), accessor.getUsername())
        );

        return storageService.loadFileAsResource(e.getStoredFilePath());
    }

    @Transactional
    public CustodyDTO.CustodyRecordDTO transferCustody(String evidenceId, EvidenceDTO.CustodyTransferRequest request, User actor) {
        Evidence e = getEvidenceEntity(evidenceId);

        CustodyRecord record = custodyLedgerService.recordCustodyEvent(
                e,
                actor,
                CustodyAction.TRANSFERRED,
                request.getTransferredTo(),
                request.getNotes() != null ? request.getNotes() : "Custody transferred by authorized investigator"
        );

        auditService.recordEvent(
                actor,
                "CUSTODY_TRANSFER",
                "Evidence",
                evidenceId,
                "127.0.0.1",
                "SUCCESS",
                String.format("Custody transferred to: %s. Notes: %s", request.getTransferredTo(), request.getNotes())
        );

        List<CustodyDTO.CustodyRecordDTO> dtos = custodyLedgerService.getCustodyHistory(e.getId());
        return dtos.get(dtos.size() - 1);
    }
}
