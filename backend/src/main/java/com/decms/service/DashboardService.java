package com.decms.service;

import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.evidence.EvidenceType;
import com.decms.domain.model.evidence.VerificationStatus;
import com.decms.domain.model.investigation.CaseStatus;
import com.decms.dto.DashboardDTO;
import com.decms.repository.AuditLogRepository;
import com.decms.repository.CaseRepository;
import com.decms.repository.EvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardService(CaseRepository caseRepository,
                            EvidenceRepository evidenceRepository,
                            AuditLogRepository auditLogRepository) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public DashboardDTO getDashboardStats() {
        DashboardDTO dto = new DashboardDTO();

        long totalCases = caseRepository.count();
        long openCases = caseRepository.countByStatus(CaseStatus.OPEN);
        long underInvCases = caseRepository.countByStatus(CaseStatus.UNDER_INVESTIGATION);
        long pendingReviewCases = caseRepository.countByStatus(CaseStatus.PENDING_REVIEW);
        long closedCases = caseRepository.countByStatus(CaseStatus.CLOSED);

        long totalEvidence = evidenceRepository.count();
        long verifiedEvidence = evidenceRepository.countByStatus(VerificationStatus.VERIFIED);
        long compromisedEvidence = evidenceRepository.countByStatus(VerificationStatus.COMPROMISED);

        dto.setTotalCases(totalCases);
        dto.setActiveCases(openCases + underInvCases + pendingReviewCases);
        dto.setClosedCases(closedCases);
        dto.setTotalEvidence(totalEvidence);
        dto.setVerifiedEvidence(verifiedEvidence);
        dto.setCompromisedEvidence(compromisedEvidence);

        double health = totalEvidence > 0 ? ((double) verifiedEvidence / totalEvidence) * 100.0 : 100.0;
        dto.setIntegrityHealthPercentage(Math.round(health * 10.0) / 10.0);

        // Group evidence by type
        Map<String, Long> evidenceByType = new HashMap<>();
        for (EvidenceType type : EvidenceType.values()) {
            evidenceByType.put(type.name(), (long) evidenceRepository.findByEvidenceType(type).size());
        }
        dto.setEvidenceByType(evidenceByType);

        // Group cases by status
        Map<String, Long> casesByStatus = new HashMap<>();
        for (CaseStatus status : CaseStatus.values()) {
            casesByStatus.put(status.name(), caseRepository.countByStatus(status));
        }
        dto.setCasesByStatus(casesByStatus);

        // Recent Audit Activities
        dto.setRecentActivities(auditLogRepository.findTop20ByOrderByTimestampDesc());

        return dto;
    }
}
