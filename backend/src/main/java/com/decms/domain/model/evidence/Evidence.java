package com.decms.domain.model.evidence;

import com.decms.domain.interfaces.Auditable;
import com.decms.domain.interfaces.Verifiable;
import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidence")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Evidence implements Verifiable, Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true, nullable = false, length = 50)
    protected String evidenceId;

    @Column(nullable = false)
    protected String fileName;

    @Column(nullable = false)
    protected String storedFilePath;

    @Column(nullable = false)
    protected Long fileSizeBytes;

    @Column(length = 100)
    protected String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    protected EvidenceType evidenceType;

    @Column(nullable = false, length = 64)
    protected String originalSha256;

    @Column(length = 64)
    protected String latestCalculatedSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    protected VerificationStatus status = VerificationStatus.VERIFIED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    protected User uploadedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    protected Case investigationCase;

    @Column(columnDefinition = "TEXT")
    protected String metadataJson;

    @Column(nullable = false, updatable = false)
    protected LocalDateTime uploadedAt;

    protected LocalDateTime lastVerifiedAt;

    public Evidence() {
        this.uploadedAt = LocalDateTime.now();
        this.lastVerifiedAt = LocalDateTime.now();
    }

    public Evidence(String evidenceId, String fileName, String storedFilePath, Long fileSizeBytes,
                    String mimeType, EvidenceType evidenceType, String originalSha256,
                    User uploadedBy, Case investigationCase) {
        this();
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.storedFilePath = storedFilePath;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.evidenceType = evidenceType;
        this.originalSha256 = originalSha256;
        this.latestCalculatedSha256 = originalSha256;
        this.status = VerificationStatus.VERIFIED;
        this.uploadedBy = uploadedBy;
        this.investigationCase = investigationCase;
    }

    // Abstract method embodying Polymorphism for sub-types
    public abstract void extractSpecificMetadata();

    @Override
    public String calculateHash() {
        return this.latestCalculatedSha256;
    }

    @Override
    public boolean verifyIntegrity(String currentHash) {
        this.latestCalculatedSha256 = currentHash;
        this.lastVerifiedAt = LocalDateTime.now();
        if (this.originalSha256 != null && this.originalSha256.equalsIgnoreCase(currentHash)) {
            this.status = VerificationStatus.VERIFIED;
            return true;
        } else {
            this.status = VerificationStatus.COMPROMISED;
            return false;
        }
    }

    @Override
    public String getHashAlgorithm() {
        return "SHA-256";
    }

    @Override
    public String generateAuditSnapshot() {
        return String.format("Evidence[id='%s', file='%s', size=%d, status=%s, sha256=%s]",
                evidenceId, fileName, fileSizeBytes, status, originalSha256);
    }

    @Override
    public String getAuditIdentifier() {
        return "EVIDENCE-" + evidenceId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvidenceId() { return evidenceId; }
    public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredFilePath() { return storedFilePath; }
    public void setStoredFilePath(String storedFilePath) { this.storedFilePath = storedFilePath; }

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

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public Case getInvestigationCase() { return investigationCase; }
    public void setInvestigationCase(Case investigationCase) { this.investigationCase = investigationCase; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
}
