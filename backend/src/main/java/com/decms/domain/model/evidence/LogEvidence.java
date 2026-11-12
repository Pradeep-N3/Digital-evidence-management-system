package com.decms.domain.model.evidence;

import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LOG")
public class LogEvidence extends Evidence {

    private Long lineCount;
    private String logSource;

    public LogEvidence() {
        super();
        this.evidenceType = EvidenceType.LOG;
    }

    public LogEvidence(String evidenceId, String fileName, String storedFilePath, Long fileSizeBytes,
                       String mimeType, String originalSha256, User uploadedBy, Case investigationCase,
                       Long lineCount, String logSource) {
        super(evidenceId, fileName, storedFilePath, fileSizeBytes, mimeType, EvidenceType.LOG, originalSha256, uploadedBy, investigationCase);
        this.lineCount = lineCount;
        this.logSource = logSource;
        extractSpecificMetadata();
    }

    @Override
    public void extractSpecificMetadata() {
        this.metadataJson = String.format("{\"lineCount\": %d, \"logSource\": \"%s\"}",
                lineCount != null ? lineCount : 0,
                logSource != null ? logSource : "System Artifact");
    }

    public Long getLineCount() { return lineCount; }
    public void setLineCount(Long lineCount) { this.lineCount = lineCount; }

    public String getLogSource() { return logSource; }
    public void setLogSource(String logSource) { this.logSource = logSource; }
}
