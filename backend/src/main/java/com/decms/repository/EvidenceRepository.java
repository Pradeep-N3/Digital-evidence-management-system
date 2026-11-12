package com.decms.repository;

import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.evidence.EvidenceType;
import com.decms.domain.model.evidence.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    Optional<Evidence> findByEvidenceId(String evidenceId);
    List<Evidence> findByInvestigationCase_IdOrderByUploadedAtDesc(Long caseId);
    List<Evidence> findAllByOrderByUploadedAtDesc();
    List<Evidence> findByStatus(VerificationStatus status);
    List<Evidence> findByEvidenceType(EvidenceType evidenceType);
    long countByStatus(VerificationStatus status);
    long count();
}
