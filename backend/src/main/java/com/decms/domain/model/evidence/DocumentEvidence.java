package com.decms.domain.model.evidence;

import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DOCUMENT")
public class DocumentEvidence extends Evidence {

    private Integer estimatedPages;
    private String documentFormat;

    public DocumentEvidence() {
        super();
        this.evidenceType = EvidenceType.DOCUMENT;
    }

    public DocumentEvidence(String evidenceId, String fileName, String storedFilePath, Long fileSizeBytes,
                            String mimeType, String originalSha256, User uploadedBy, Case investigationCase,
                            Integer estimatedPages, String documentFormat) {
        super(evidenceId, fileName, storedFilePath, fileSizeBytes, mimeType, EvidenceType.DOCUMENT, originalSha256, uploadedBy, investigationCase);
        this.estimatedPages = estimatedPages;
        this.documentFormat = documentFormat;
        extractSpecificMetadata();
    }

    @Override
    public void extractSpecificMetadata() {
        this.metadataJson = String.format("{\"documentFormat\": \"%s\", \"pages\": %d}",
                documentFormat != null ? documentFormat : "Standard Document",
                estimatedPages != null ? estimatedPages : 1);
    }

    public Integer getEstimatedPages() { return estimatedPages; }
    public void setEstimatedPages(Integer estimatedPages) { this.estimatedPages = estimatedPages; }

    public String getDocumentFormat() { return documentFormat; }
    public void setDocumentFormat(String documentFormat) { this.documentFormat = documentFormat; }
}
