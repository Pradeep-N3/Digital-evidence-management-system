package com.decms.domain.model.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AUDITOR")
public class Auditor extends User {

    private String certificationNumber;

    public Auditor() {
        super();
        this.role = Role.ROLE_AUDITOR;
    }

    public Auditor(String username, String email, String passwordHash, String fullName, String badgeNumber, String department, String certificationNumber) {
        super(username, email, passwordHash, fullName, Role.ROLE_AUDITOR, badgeNumber, department);
        this.certificationNumber = certificationNumber;
    }

    @Override
    public boolean hasPermission(String permission) {
        // Auditors have read-only oversight permissions
        if (permission.startsWith("VIEW_") || permission.equals("VERIFY_INTEGRITY") || permission.equals("EXPORT_REPORTS")) {
            return true;
        }
        return false;
    }

    public String getCertificationNumber() { return certificationNumber; }
    public void setCertificationNumber(String certificationNumber) { this.certificationNumber = certificationNumber; }
}
