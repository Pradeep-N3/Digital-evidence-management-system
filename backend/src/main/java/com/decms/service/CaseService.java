package com.decms.service;

import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.investigation.CaseStatus;
import com.decms.domain.model.user.User;
import com.decms.dto.AuthDTO;
import com.decms.dto.CaseDTO;
import com.decms.dto.EvidenceDTO;
import com.decms.exception.ResourceNotFoundException;
import com.decms.repository.CaseRepository;
import com.decms.repository.EvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final AuditService auditService;
    private final AuthService authService;

    public CaseService(CaseRepository caseRepository,
                       EvidenceRepository evidenceRepository,
                       AuditService auditService,
                       AuthService authService) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.auditService = auditService;
        this.authService = authService;
    }

    private synchronized String generateNextCaseId() {
        int year = Year.now().getValue();
        long count = caseRepository.count() + 1;
        return String.format("CASE-%d-%04d", year, count);
    }

    @Transactional
    public CaseDTO.CaseResponseDTO createCase(CaseDTO.CreateCaseRequest request, User leadInvestigator) {
        String uniqueCaseId = generateNextCaseId();
        Case newCase = new Case(
                uniqueCaseId,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                leadInvestigator,
                request.getIncidentDate() != null ? request.getIncidentDate() : LocalDateTime.now()
        );

        Case savedCase = caseRepository.save(newCase);
        auditService.recordEvent(
                leadInvestigator,
                "CASE_CREATED",
                "Case",
                savedCase.getCaseId(),
                "127.0.0.1",
                "SUCCESS",
                String.format("New Case opened: '%s' [%s]", savedCase.getTitle(), savedCase.getCaseId())
        );

        return mapToDTO(savedCase, true);
    }

    public List<CaseDTO.CaseResponseDTO> getAllCases() {
        List<Case> cases = caseRepository.findAllByOrderByCreatedAtDesc();
        List<CaseDTO.CaseResponseDTO> dtos = new ArrayList<>();
        for (Case c : cases) {
            dtos.add(mapToDTO(c, false));
        }
        return dtos;
    }

    public Case getCaseEntity(String caseId) {
        return caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));
    }

    public CaseDTO.CaseResponseDTO getCaseById(String caseId) {
        Case c = getCaseEntity(caseId);
        return mapToDTO(c, true);
    }

    @Transactional
    public CaseDTO.CaseResponseDTO updateCaseStatus(String caseId, CaseStatus newStatus, User actor) {
        Case c = getCaseEntity(caseId);
        CaseStatus oldStatus = c.getStatus();
        c.setStatus(newStatus);
        Case updated = caseRepository.save(c);

        auditService.recordEvent(
                actor,
                "CASE_STATUS_CHANGED",
                "Case",
                caseId,
                "127.0.0.1",
                "SUCCESS",
                String.format("Case status transitioned from %s to %s", oldStatus, newStatus)
        );

        return mapToDTO(updated, true);
    }

    public CaseDTO.CaseResponseDTO mapToDTO(Case c, boolean includeEvidence) {
        CaseDTO.CaseResponseDTO dto = new CaseDTO.CaseResponseDTO();
        dto.setId(c.getId());
        dto.setCaseId(c.getCaseId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setStatus(c.getStatus());
        dto.setPriority(c.getPriority());
        dto.setIncidentDate(c.getIncidentDate());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        if (c.getLeadInvestigator() != null) {
            dto.setLeadInvestigator(authService.mapToUserDTO(c.getLeadInvestigator()));
        }

        List<Evidence> evidenceList = evidenceRepository.findByInvestigationCase_IdOrderByUploadedAtDesc(c.getId());
        dto.setEvidenceCount(evidenceList.size());

        if (includeEvidence) {
            List<EvidenceDTO.EvidenceSummaryDTO> evidenceDtos = new ArrayList<>();
            for (Evidence e : evidenceList) {
                evidenceDtos.add(mapEvidenceToSummary(e));
            }
            dto.setEvidenceList(evidenceDtos);
        }

        return dto;
    }

    public EvidenceDTO.EvidenceSummaryDTO mapEvidenceToSummary(Evidence e) {
        EvidenceDTO.EvidenceSummaryDTO dto = new EvidenceDTO.EvidenceSummaryDTO();
        dto.setId(e.getId());
        dto.setEvidenceId(e.getEvidenceId());
        dto.setFileName(e.getFileName());
        dto.setFileSizeBytes(e.getFileSizeBytes());
        dto.setMimeType(e.getMimeType());
        dto.setEvidenceType(e.getEvidenceType());
        dto.setOriginalSha256(e.getOriginalSha256());
        dto.setLatestCalculatedSha256(e.getLatestCalculatedSha256());
        dto.setStatus(e.getStatus());
        dto.setUploadedByUsername(e.getUploadedBy() != null ? e.getUploadedBy().getUsername() : "UNKNOWN");
        dto.setCaseId(e.getInvestigationCase() != null ? e.getInvestigationCase().getCaseId() : "N/A");
        dto.setCaseTitle(e.getInvestigationCase() != null ? e.getInvestigationCase().getTitle() : "N/A");
        dto.setMetadataJson(e.getMetadataJson());
        dto.setUploadedAt(e.getUploadedAt());
        dto.setLastVerifiedAt(e.getLastVerifiedAt());
        return dto;
    }
}
