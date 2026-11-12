package com.decms.repository;

import com.decms.domain.model.custody.CustodyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustodyRecordRepository extends JpaRepository<CustodyRecord, Long> {
    List<CustodyRecord> findByEvidence_IdOrderByIdAsc(Long evidenceId);
    Optional<CustodyRecord> findTopByEvidence_IdOrderByIdDesc(Long evidenceId);
    List<CustodyRecord> findAllByOrderByIdDesc();
}
