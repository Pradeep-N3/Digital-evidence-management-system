package com.decms.domain.model.custody;

import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.evidence.VerificationStatus;
import com.decms.domain.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "integrity_logs")
public class IntegrityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "verified_by_id", nullable = false)
    private User verifiedBy;

    @Column(nullable = false, length = 64)
    private String originalHash;

    @Column(nullable = false, length = 64)
    private String calculatedHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus resultStatus;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime verifiedAt;

    public IntegrityLog() {
        this.verifiedAt = LocalDateTime.now();
    }

    public IntegrityLog(Evidence evidence, User verifiedBy, String originalHash, String calculatedHash,
                        VerificationStatus resultStatus, String remarks) {
        this();
        this.evidence = evidence;
        this.verifiedBy = verifiedBy;
        this.originalHash = originalHash;
        this.calculatedHash = calculatedHash;
        this.resultStatus = resultStatus;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Evidence getEvidence() { return evidence; }
    public void setEvidence(Evidence evidence) { this.evidence = evidence; }

    public User getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(User verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getOriginalHash() { return originalHash; }
    public void setOriginalHash(String originalHash) { this.originalHash = originalHash; }

    public String getCalculatedHash() { return calculatedHash; }
    public void setCalculatedHash(String calculatedHash) { this.calculatedHash = calculatedHash; }

    public VerificationStatus getResultStatus() { return resultStatus; }
    public void setResultStatus(VerificationStatus resultStatus) { this.resultStatus = resultStatus; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
