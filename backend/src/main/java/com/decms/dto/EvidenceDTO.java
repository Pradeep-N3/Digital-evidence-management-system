package com.decms.dto;

import com.decms.domain.model.evidence.EvidenceType;
import com.decms.domain.model.evidence.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class EvidenceDTO {

    public static class CustodyTransferRequest {
        @NotBlank(message = "Recipient identity is required")
        private String transferredTo;
        private String notes;

        public String getTransferredTo() { return transferredTo; }
        public void setTransferredTo(String transferredTo) { this.transferredTo = transferredTo; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class EvidenceSummaryDTO {
        private Long id;
        private String evidenceId;
        private String fileName;
        private Long fileSizeBytes;
        private String mimeType;
        private EvidenceType evidenceType;
        private String originalSha256;
        private String latestCalculatedSha256;
        private VerificationStatus status;
        private String uploadedByUsername;
        private String caseId;
        private String caseTitle;
        private String metadataJson;
        private LocalDateTime uploadedAt;
        private LocalDateTime lastVerifiedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEvidenceId() { return evidenceId; }
        public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public Long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        public EvidenceType getEvidenceType() { return evidenceType; }
        public void setEvidenceType(EvidenceType evidenceType) { this.evidenceType = evidenceType; }
        public String getOriginalSha256() { return originalSha256; }
        public void setOriginalSha256(String originalSha256) { this.originalSha256 = originalSha256; }
        public String getLatestCalculatedSha256() { return latestCalculatedSha256; }
        public void setLatestCalculatedSha256(String latestCalculatedSha256) { this.latestCalculatedSha256 = latestCalculatedSha256; }
        public VerificationStatus getStatus() { return status; }
        public void setStatus(VerificationStatus status) { this.status = status; }
        public String getUploadedByUsername() { return uploadedByUsername; }
        public void setUploadedByUsername(String uploadedByUsername) { this.uploadedByUsername = uploadedByUsername; }
        public String getCaseId() { return caseId; }
        public void setCaseId(String caseId) { this.caseId = caseId; }
        public String getCaseTitle() { return caseTitle; }
        public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
        public String getMetadataJson() { return metadataJson; }
        public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
        public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
        public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
    }

    public static class IntegrityCheckResponseDTO {
        private String evidenceId;
        private String fileName;
        private String originalHash;
        private String calculatedHash;
        private VerificationStatus status;
        private boolean match;
        private LocalDateTime verifiedAt;
        private String verifiedByUsername;

        public String getEvidenceId() { return evidenceId; }
        public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getOriginalHash() { return originalHash; }
        public void setOriginalHash(String originalHash) { this.originalHash = originalHash; }
        public String getCalculatedHash() { return calculatedHash; }
        public void setCalculatedHash(String calculatedHash) { this.calculatedHash = calculatedHash; }
        public VerificationStatus getStatus() { return status; }
        public void setStatus(VerificationStatus status) { this.status = status; }
        public boolean isMatch() { return match; }
        public void setMatch(boolean match) { this.match = match; }
        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
        public String getVerifiedByUsername() { return verifiedByUsername; }
        public void setVerifiedByUsername(String verifiedByUsername) { this.verifiedByUsername = verifiedByUsername; }
    }
}
