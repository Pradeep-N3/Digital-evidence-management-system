package com.decms.domain.model.investigation;

import com.decms.domain.interfaces.Auditable;
import com.decms.domain.interfaces.Exportable;
import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "investigation_cases")
public class Case implements Auditable, Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String caseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatus status = CaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CasePriority priority = CasePriority.MEDIUM;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lead_investigator_id", nullable = false)
    private User leadInvestigator;

    private LocalDateTime incidentDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "investigationCase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Evidence> evidenceList = new ArrayList<>();

    public Case() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Case(String caseId, String title, String description, CasePriority priority, User leadInvestigator, LocalDateTime incidentDate) {
        this();
        this.caseId = caseId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.leadInvestigator = leadInvestigator;
        this.incidentDate = incidentDate;
        this.status = CaseStatus.OPEN;
    }

    public void addEvidence(Evidence evidence) {
        evidenceList.add(evidence);
        evidence.setInvestigationCase(this);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeEvidence(Evidence evidence) {
        evidenceList.remove(evidence);
        evidence.setInvestigationCase(null);
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String generateAuditSnapshot() {
        return String.format("Case[id='%s', title='%s', status=%s, priority=%s, evidenceCount=%d]",
                caseId, title, status, priority, evidenceList.size());
    }

    @Override
    public String getAuditIdentifier() {
        return "CASE-" + caseId;
    }

    @Override
    public Map<String, Object> getExportMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("caseId", caseId);
        meta.put("title", title);
        meta.put("description", description);
        meta.put("status", status.name());
        meta.put("priority", priority.name());
        meta.put("leadInvestigator", leadInvestigator != null ? leadInvestigator.getFullName() : "N/A");
        meta.put("createdAt", createdAt.toString());
        meta.put("incidentDate", incidentDate != null ? incidentDate.toString() : "N/A");
        meta.put("evidenceCount", evidenceList != null ? evidenceList.size() : 0);
        return meta;
    }

    @Override
    public String getExportTitle() {
        return "FORENSIC CASE INVESTIGATION REPORT - " + caseId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public CasePriority getPriority() { return priority; }
    public void setPriority(CasePriority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public User getLeadInvestigator() { return leadInvestigator; }
    public void setLeadInvestigator(User leadInvestigator) { this.leadInvestigator = leadInvestigator; }

    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Evidence> getEvidenceList() { return evidenceList; }
    public void setEvidenceList(List<Evidence> evidenceList) { this.evidenceList = evidenceList; }
}
