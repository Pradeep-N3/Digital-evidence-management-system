package com.decms.service;

import com.decms.domain.model.audit.AuditLog;
import com.decms.domain.model.user.User;
import com.decms.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog recordEvent(User user, String actionType, String targetEntity, String targetId,
                                String ipAddress, String status, String details) {
        AuditLog log = new AuditLog(
                user,
                user != null ? user.getUsername() : "ANONYMOUS",
                actionType,
                targetEntity,
                targetId,
                ipAddress,
                status,
                details
        );
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop20ByOrderByTimestampDesc();
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
