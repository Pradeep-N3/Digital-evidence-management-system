package com.decms;

import com.decms.domain.model.custody.CustodyAction;
import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.evidence.VerificationStatus;
import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.investigation.CasePriority;
import com.decms.domain.model.investigation.CaseStatus;
import com.decms.domain.model.user.Investigator;
import com.decms.domain.model.user.User;
import com.decms.dto.CustodyDTO;
import com.decms.dto.EvidenceDTO;
import com.decms.repository.CaseRepository;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.UserRepository;
import com.decms.service.CustodyLedgerService;
import com.decms.service.EvidenceService;
import com.decms.service.HashVerificationService;
import com.decms.service.ReportGenerationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("dev")
class DecmsApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private HashVerificationService hashVerificationService;

    @Autowired
    private CustodyLedgerService custodyLedgerService;

    @Autowired
    private ReportGenerationService reportGenerationService;

    @Autowired
    private com.decms.service.AuthService authService;

    @Test
    void testEndToEndForensicLifecycle() throws Exception {
        // 0. Verify Seeded Login
        com.decms.dto.AuthDTO.LoginRequest adminLogin = new com.decms.dto.AuthDTO.LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("admin123");
        com.decms.dto.AuthDTO.AuthResponse adminResp = authService.login(adminLogin);
        Assertions.assertNotNull(adminResp.getToken(), "Admin login should generate JWT token");

        com.decms.dto.AuthDTO.LoginRequest invLogin = new com.decms.dto.AuthDTO.LoginRequest();
        invLogin.setUsername("investigator");
        invLogin.setPassword("investigator123");
        com.decms.dto.AuthDTO.AuthResponse invResp = authService.login(invLogin);
        Assertions.assertNotNull(invResp.getToken(), "Investigator login should generate JWT token");

        // 1. Verify Seeded Investigator
        User investigator = userRepository.findByUsername("investigator")
                .orElseThrow(() -> new AssertionError("Investigator seed not found"));
        Assertions.assertNotNull(investigator);

        // 2. Open Case
        Case forensicCase = new Case(
                "CASE-2026-TEST",
                "Automated Network Intrusion Test",
                "Incident involving rogue outbound data transfers",
                CasePriority.HIGH,
                investigator,
                LocalDateTime.now()
        );
        forensicCase = caseRepository.save(forensicCase);
        Assertions.assertEquals("CASE-2026-TEST", forensicCase.getCaseId());

        // 3. Upload & Ingest Digital Evidence
        byte[] originalContent = "CRITICAL FORENSIC LOG: SUSPICIOUS PACKET DUMP 0xDEADBEEF".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "packet_capture.log",
                "text/plain",
                originalContent
        );

        EvidenceDTO.EvidenceSummaryDTO uploadedDto = evidenceService.uploadEvidence(forensicCase.getCaseId(), file, investigator);
        Assertions.assertNotNull(uploadedDto.getOriginalSha256());
        Assertions.assertEquals(VerificationStatus.VERIFIED, uploadedDto.getStatus());

        Evidence evidence = evidenceService.getEvidenceEntity(uploadedDto.getEvidenceId());

        // 4. Verify Integrity (Should Pass initially)
        EvidenceDTO.IntegrityCheckResponseDTO initialCheck = hashVerificationService.verifyEvidence(evidence, investigator);
        Assertions.assertTrue(initialCheck.isMatch());
        Assertions.assertEquals(VerificationStatus.VERIFIED, initialCheck.getStatus());

        // 5. Transfer Custody & Validate Ledger Block Linking
        EvidenceDTO.CustodyTransferRequest transferReq = new EvidenceDTO.CustodyTransferRequest();
        transferReq.setTransferredTo("State Forensic Lab Officer Harris");
        transferReq.setNotes("Transferred for deep packet analysis");
        evidenceService.transferCustody(evidence.getEvidenceId(), transferReq, investigator);

        CustodyDTO.ChainValidationResultDTO chainResult = custodyLedgerService.validateEvidenceChain(evidence);
        System.out.println("DEBUG CHAIN VALIDATION ISSUES: " + chainResult.getIssues());
        Assertions.assertTrue(chainResult.isEntireChainValid(), "Custody ledger chain should be 100% valid. Issues: " + chainResult.getIssues());
        Assertions.assertEquals(3, chainResult.getTotalBlocks()); // UPLOADED + VERIFIED + TRANSFERRED

        // 6. Simulate Tampering on Disk
        File physicalFile = new File(evidence.getStoredFilePath());
        try (FileOutputStream fos = new FileOutputStream(physicalFile, true)) {
            fos.write("TAMPERED_INJECTED_BYTES".getBytes(StandardCharsets.UTF_8));
        }

        // 7. Verify Integrity Again (Should Detect Tampering!)
        EvidenceDTO.IntegrityCheckResponseDTO tamperedCheck = hashVerificationService.verifyEvidence(evidence, investigator);
        Assertions.assertFalse(tamperedCheck.isMatch(), "Tampered file must trigger hash mismatch!");
        Assertions.assertEquals(VerificationStatus.COMPROMISED, tamperedCheck.getStatus());

        // 8. Generate Court-Ready PDF Report
        byte[] pdfReport = reportGenerationService.generateCasePdfReport(forensicCase.getCaseId());
        Assertions.assertNotNull(pdfReport);
        Assertions.assertTrue(pdfReport.length > 500, "PDF should contain valid report data");
        String pdfHeader = new String(pdfReport, 0, 4, StandardCharsets.US_ASCII);
        Assertions.assertEquals("%PDF", pdfHeader, "Report must be a valid PDF document");
    }
}
