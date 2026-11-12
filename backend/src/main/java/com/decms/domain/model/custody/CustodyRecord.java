package com.decms.domain.model.custody;

import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.user.User;
import com.decms.util.CryptoUtil;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "custody_records")
public class CustodyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustodyAction action;

    @Column(length = 150)
    private String transferredTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 100)
    private String previousRecordHash;

    @Column(nullable = false, length = 100)
    private String recordHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    private static final DateTimeFormatter BLOCK_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public CustodyRecord() {
        this.timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public CustodyRecord(Evidence evidence, User performedBy, CustodyAction action,
                         String transferredTo, String notes, String previousRecordHash) {
        this();
        this.evidence = evidence;
        this.performedBy = performedBy;
        this.action = action;
        this.transferredTo = transferredTo;
        this.notes = notes;
        this.previousRecordHash = previousRecordHash != null ? previousRecordHash : "0000000000000000000000000000000000000000000000000000000000000000";
        this.recordHash = calculateBlockChecksum();
    }

    public String calculateBlockChecksum() {
        String formattedTimestamp = this.timestamp != null ? this.timestamp.format(BLOCK_TIME_FORMATTER) : "";
        String payload = String.format("%s|%d|%s|%d|%s|%s|%s",
                this.previousRecordHash,
                this.evidence != null ? this.evidence.getId() : 0,
                this.action != null ? this.action.name() : "",
                this.performedBy != null ? this.performedBy.getId() : 0,
                this.transferredTo != null ? this.transferredTo : "",
                formattedTimestamp,
                this.notes != null ? this.notes : "");
        return CryptoUtil.sha256Hex(payload);
    }

    public boolean verifyBlockIntegrity() {
        String recalculated = calculateBlockChecksum();
        return this.recordHash != null && this.recordHash.equalsIgnoreCase(recalculated);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Evidence getEvidence() { return evidence; }
    public void setEvidence(Evidence evidence) { this.evidence = evidence; }

    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }

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

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
