package com.decms.repository;

import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.investigation.CaseStatus;
import com.decms.domain.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    Optional<Case> findByCaseId(String caseId);
    List<Case> findAllByOrderByCreatedAtDesc();
    List<Case> findByStatus(CaseStatus status);
    List<Case> findByLeadInvestigator(User investigator);
    long countByStatus(CaseStatus status);
    long count();
}
