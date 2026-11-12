package com.decms.controller;

import com.decms.domain.model.audit.AuditLog;
import com.decms.domain.model.evidence.Evidence;
import com.decms.dto.CustodyDTO;
import com.decms.dto.EvidenceDTO;
import com.decms.service.AuditService;
import com.decms.service.CustodyLedgerService;
import com.decms.service.EvidenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;
    private final CustodyLedgerService custodyLedgerService;
    private final EvidenceService evidenceService;

    public AuditController(AuditService auditService,
                           CustodyLedgerService custodyLedgerService,
                           EvidenceService evidenceService) {
        this.auditService = auditService;
        this.custodyLedgerService = custodyLedgerService;
        this.evidenceService = evidenceService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/custody-records")
    public ResponseEntity<List<CustodyDTO.CustodyRecordDTO>> getAllCustodyRecords() {
        return ResponseEntity.ok(custodyLedgerService.getAllCustodyRecords());
    }

    @GetMapping("/validate-ledger/{evidenceId}")
    public ResponseEntity<CustodyDTO.ChainValidationResultDTO> validateLedger(@PathVariable String evidenceId) {
        Evidence evidence = evidenceService.getEvidenceEntity(evidenceId);
        return ResponseEntity.ok(custodyLedgerService.validateEvidenceChain(evidence));
    }

    @GetMapping("/validate-all")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<CustodyDTO.ChainValidationResultDTO>> validateAllLedgers() {
        List<EvidenceDTO.EvidenceSummaryDTO> all = evidenceService.getAllEvidence();
        List<CustodyDTO.ChainValidationResultDTO> results = new ArrayList<>();
        for (EvidenceDTO.EvidenceSummaryDTO s : all) {
            Evidence e = evidenceService.getEvidenceEntity(s.getEvidenceId());
            results.add(custodyLedgerService.validateEvidenceChain(e));
        }
        return ResponseEntity.ok(results);
    }
}
