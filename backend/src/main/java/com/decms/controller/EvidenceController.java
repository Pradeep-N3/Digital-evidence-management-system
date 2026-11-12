package com.decms.controller;

import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.user.User;
import com.decms.dto.CustodyDTO;
import com.decms.dto.EvidenceDTO;
import com.decms.service.AuthService;
import com.decms.service.CustodyLedgerService;
import com.decms.service.EvidenceService;
import com.decms.service.HashVerificationService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final HashVerificationService hashVerificationService;
    private final CustodyLedgerService custodyLedgerService;
    private final AuthService authService;

    public EvidenceController(EvidenceService evidenceService,
                              HashVerificationService hashVerificationService,
                              CustodyLedgerService custodyLedgerService,
                              AuthService authService) {
        this.evidenceService = evidenceService;
        this.hashVerificationService = hashVerificationService;
        this.custodyLedgerService = custodyLedgerService;
        this.authService = authService;
    }

    @GetMapping("/evidence")
    public ResponseEntity<List<EvidenceDTO.EvidenceSummaryDTO>> getAllEvidence() {
        return ResponseEntity.ok(evidenceService.getAllEvidence());
    }

    @PostMapping(value = "/cases/{caseId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'ADMIN')")
    public ResponseEntity<EvidenceDTO.EvidenceSummaryDTO> uploadEvidence(
            @PathVariable String caseId,
            @RequestParam("file") MultipartFile file) {
        User uploader = authService.getCurrentlyAuthenticatedUser();
        return ResponseEntity.ok(evidenceService.uploadEvidence(caseId, file, uploader));
    }

    @GetMapping("/evidence/{evidenceId}")
    public ResponseEntity<EvidenceDTO.EvidenceSummaryDTO> getEvidenceById(@PathVariable String evidenceId) {
        return ResponseEntity.ok(evidenceService.getEvidenceById(evidenceId));
    }

    @GetMapping("/evidence/{evidenceId}/download")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable String evidenceId) {
        User accessor = authService.getCurrentlyAuthenticatedUser();
        Evidence entity = evidenceService.getEvidenceEntity(evidenceId);
        Resource file = evidenceService.getEvidenceFileResource(evidenceId, accessor);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entity.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @PostMapping("/evidence/{evidenceId}/verify")
    public ResponseEntity<EvidenceDTO.IntegrityCheckResponseDTO> verifyEvidenceIntegrity(@PathVariable String evidenceId) {
        User verifier = authService.getCurrentlyAuthenticatedUser();
        Evidence entity = evidenceService.getEvidenceEntity(evidenceId);
        return ResponseEntity.ok(hashVerificationService.verifyEvidence(entity, verifier));
    }

    @GetMapping("/evidence/{evidenceId}/custody")
    public ResponseEntity<List<CustodyDTO.CustodyRecordDTO>> getCustodyChain(@PathVariable String evidenceId) {
        Evidence entity = evidenceService.getEvidenceEntity(evidenceId);
        return ResponseEntity.ok(custodyLedgerService.getCustodyHistory(entity.getId()));
    }

    @PostMapping("/evidence/{evidenceId}/transfer")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'ADMIN')")
    public ResponseEntity<CustodyDTO.CustodyRecordDTO> transferCustody(
            @PathVariable String evidenceId,
            @Valid @RequestBody EvidenceDTO.CustodyTransferRequest request) {
        User actor = authService.getCurrentlyAuthenticatedUser();
        return ResponseEntity.ok(evidenceService.transferCustody(evidenceId, request, actor));
    }
}
