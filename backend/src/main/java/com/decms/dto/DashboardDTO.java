package com.decms.dto;

import com.decms.domain.model.audit.AuditLog;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private long totalCases;
    private long activeCases;
    private long closedCases;
    private long totalEvidence;
    private long verifiedEvidence;
    private long compromisedEvidence;
    private double integrityHealthPercentage;
    private Map<String, Long> evidenceByType;
    private Map<String, Long> casesByStatus;
    private List<AuditLog> recentActivities;

    public long getTotalCases() { return totalCases; }
    public void setTotalCases(long totalCases) { this.totalCases = totalCases; }
    public long getActiveCases() { return activeCases; }
    public void setActiveCases(long activeCases) { this.activeCases = activeCases; }
    public long getClosedCases() { return closedCases; }
    public void setClosedCases(long closedCases) { this.closedCases = closedCases; }
    public long getTotalEvidence() { return totalEvidence; }
    public void setTotalEvidence(long totalEvidence) { this.totalEvidence = totalEvidence; }
    public long getVerifiedEvidence() { return verifiedEvidence; }
    public void setVerifiedEvidence(long verifiedEvidence) { this.verifiedEvidence = verifiedEvidence; }
    public long getCompromisedEvidence() { return compromisedEvidence; }
    public void setCompromisedEvidence(long compromisedEvidence) { this.compromisedEvidence = compromisedEvidence; }
    public double getIntegrityHealthPercentage() { return integrityHealthPercentage; }
    public void setIntegrityHealthPercentage(double integrityHealthPercentage) { this.integrityHealthPercentage = integrityHealthPercentage; }
    public Map<String, Long> getEvidenceByType() { return evidenceByType; }
    public void setEvidenceByType(Map<String, Long> evidenceByType) { this.evidenceByType = evidenceByType; }
    public Map<String, Long> getCasesByStatus() { return casesByStatus; }
    public void setCasesByStatus(Map<String, Long> casesByStatus) { this.casesByStatus = casesByStatus; }
    public List<AuditLog> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<AuditLog> recentActivities) { this.recentActivities = recentActivities; }
}
