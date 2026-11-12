package com.decms.dto;

import com.decms.domain.model.custody.CustodyAction;
import java.time.LocalDateTime;
import java.util.List;

public class CustodyDTO {

    public static class CustodyRecordDTO {
        private Long id;
        private String evidenceId;
        private String evidenceFileName;
        private String performedByUsername;
        private String performedByFullName;
        private CustodyAction action;
        private String transferredTo;
        private String notes;
        private String previousRecordHash;
        private String recordHash;
        private boolean blockIntegrityValid;
        private LocalDateTime timestamp;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEvidenceId() { return evidenceId; }
        public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
        public String getEvidenceFileName() { return evidenceFileName; }
        public void setEvidenceFileName(String evidenceFileName) { this.evidenceFileName = evidenceFileName; }
        public String getPerformedByUsername() { return performedByUsername; }
        public void setPerformedByUsername(String performedByUsername) { this.performedByUsername = performedByUsername; }
        public String getPerformedByFullName() { return performedByFullName; }
        public void setPerformedByFullName(String performedByFullName) { this.performedByFullName = performedByFullName; }
        public CustodyAction getAction() { return action; }
        public void setAction(CustodyAction action) { this.action = action; }
        public String getTransferredTo() { return transferredTo; }
        public void setTransferredTo(String transferredTo) { this.transferredTo = transferredTo; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getPreviousRecordHash() { return previousRecordHash; }
        public void setPreviousRecordHash(String previousRecordHash) { this.previousRecordHash = previousRecordHash; }
        public String getRecordHash() { return recordHash; }
        public void setRecordHash(String recordHash) { this.recordHash = recordHash; }
        public boolean isBlockIntegrityValid() { return blockIntegrityValid; }
        public void setBlockIntegrityValid(boolean blockIntegrityValid) { this.blockIntegrityValid = blockIntegrityValid; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class ChainValidationResultDTO {
        private String evidenceId;
        private boolean entireChainValid;
        private int totalBlocks;
        private int validBlocks;
        private List<String> issues;

        public String getEvidenceId() { return evidenceId; }
        public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
        public boolean isEntireChainValid() { return entireChainValid; }
        public void setEntireChainValid(boolean entireChainValid) { this.entireChainValid = entireChainValid; }
        public int getTotalBlocks() { return totalBlocks; }
        public void setTotalBlocks(int totalBlocks) { this.totalBlocks = totalBlocks; }
        public int getValidBlocks() { return validBlocks; }
        public void setValidBlocks(int validBlocks) { this.validBlocks = validBlocks; }
        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
    }
}
