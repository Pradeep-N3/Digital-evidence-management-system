package com.decms.domain.model.evidence;

import com.decms.domain.model.investigation.Case;
import com.decms.domain.model.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("IMAGE")
public class ImageEvidence extends Evidence {

    private Integer imageWidth;
    private Integer imageHeight;
    private String colorSpace;

    public ImageEvidence() {
        super();
        this.evidenceType = EvidenceType.IMAGE;
    }

    public ImageEvidence(String evidenceId, String fileName, String storedFilePath, Long fileSizeBytes,
                         String mimeType, String originalSha256, User uploadedBy, Case investigationCase,
                         Integer imageWidth, Integer imageHeight, String colorSpace) {
        super(evidenceId, fileName, storedFilePath, fileSizeBytes, mimeType, EvidenceType.IMAGE, originalSha256, uploadedBy, investigationCase);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.colorSpace = colorSpace != null ? colorSpace : "sRGB";
        extractSpecificMetadata();
    }

    @Override
    public void extractSpecificMetadata() {
        if (imageWidth != null && imageHeight != null) {
            this.metadataJson = String.format("{\"dimensions\": \"%dx%d\", \"colorSpace\": \"%s\"}",
                    imageWidth, imageHeight, colorSpace != null ? colorSpace : "sRGB");
        }
    }

    public Integer getImageWidth() { return imageWidth; }
    public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }

    public Integer getImageHeight() { return imageHeight; }
    public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }

    public String getColorSpace() { return colorSpace; }
    public void setColorSpace(String colorSpace) { this.colorSpace = colorSpace; }
}
