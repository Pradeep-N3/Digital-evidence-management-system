package com.decms.domain.interfaces;

public interface Auditable {
    String generateAuditSnapshot();
    String getAuditIdentifier();
}
