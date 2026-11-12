package com.decms.domain.model.evidence;

import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MEDIA")
public class MediaEvidence extends Evidence {

    private Long durationSeconds;
    private String mediaCodec;

    public MediaEvidence() {
        super();
        this.evidenceType = EvidenceType.MEDIA;
    }

    public MediaEvidence(String evidenceId, String fileName, String storedFilePath, Long fileSizeBytes,
                         String mimeType, String originalSha256, User uploadedBy, Case investigationCase,
                         Long durationSeconds, String mediaCodec) {
        super(evidenceId, fileName, storedFilePath, fileSizeBytes, mimeType, EvidenceType.MEDIA, originalSha256, uploadedBy, investigationCase);
        this.durationSeconds = durationSeconds;
        this.mediaCodec = mediaCodec;
        extractSpecificMetadata();
    }

    @Override
    public void extractSpecificMetadata() {
        this.metadataJson = String.format("{\"durationSeconds\": %d, \"codec\": \"%s\"}",
                durationSeconds != null ? durationSeconds : 0,
                mediaCodec != null ? mediaCodec : "Standard");
    }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getMediaCodec() { return mediaCodec; }
    public void setMediaCodec(String mediaCodec) { this.mediaCodec = mediaCodec; }
}
