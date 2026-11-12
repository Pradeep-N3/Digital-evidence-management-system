package com.decms.repository;

import com.decms.domain.model.custody.IntegrityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrityLogRepository extends JpaRepository<IntegrityLog, Long> {
    List<IntegrityLog> findByEvidence_IdOrderByVerifiedAtDesc(Long evidenceId);
    List<IntegrityLog> findAllByOrderByVerifiedAtDesc();
}
