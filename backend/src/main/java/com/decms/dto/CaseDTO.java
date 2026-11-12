package com.decms.dto;

import com.decms.domain.model.investigation.CasePriority;
import com.decms.domain.model.investigation.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CaseDTO {

    public static class CreateCaseRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        @NotNull(message = "Priority is required")
        private CasePriority priority;
        private LocalDateTime incidentDate;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public CasePriority getPriority() { return priority; }
        public void setPriority(CasePriority priority) { this.priority = priority; }
        public LocalDateTime getIncidentDate() { return incidentDate; }
        public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }
    }

    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private CaseStatus status;

        public CaseStatus getStatus() { return status; }
        public void setStatus(CaseStatus status) { this.status = status; }
    }

    public static class CaseResponseDTO {
        private Long id;
        private String caseId;
        private String title;
        private String description;
        private CaseStatus status;
        private CasePriority priority;
        private AuthDTO.UserDTO leadInvestigator;
        private LocalDateTime incidentDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int evidenceCount;
        private List<EvidenceDTO.EvidenceSummaryDTO> evidenceList;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCaseId() { return caseId; }
        public void setCaseId(String caseId) { this.caseId = caseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public CaseStatus getStatus() { return status; }
        public void setStatus(CaseStatus status) { this.status = status; }
        public CasePriority getPriority() { return priority; }
        public void setPriority(CasePriority priority) { this.priority = priority; }
        public AuthDTO.UserDTO getLeadInvestigator() { return leadInvestigator; }
        public void setLeadInvestigator(AuthDTO.UserDTO leadInvestigator) { this.leadInvestigator = leadInvestigator; }
        public LocalDateTime getIncidentDate() { return incidentDate; }
        public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public int getEvidenceCount() { return evidenceCount; }
        public void setEvidenceCount(int evidenceCount) { this.evidenceCount = evidenceCount; }
        public List<EvidenceDTO.EvidenceSummaryDTO> getEvidenceList() { return evidenceList; }
        public void setEvidenceList(List<EvidenceDTO.EvidenceSummaryDTO> evidenceList) { this.evidenceList = evidenceList; }
    }
}
