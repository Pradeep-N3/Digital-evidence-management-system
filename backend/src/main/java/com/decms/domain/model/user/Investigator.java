package com.decms.domain.model.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INVESTIGATOR")
public class Investigator extends User {

    public Investigator() {
        super();
        this.role = Role.ROLE_INVESTIGATOR;
    }

    public Investigator(String username, String email, String passwordHash, String fullName, String badgeNumber, String department) {
        super(username, email, passwordHash, fullName, Role.ROLE_INVESTIGATOR, badgeNumber, department);
    }

    @Override
    public boolean hasPermission(String permission) {
        if ("SYSTEM_USER_MANAGE".equals(permission) || "PURGE_AUDIT_LOGS".equals(permission)) {
            return false;
        }
        return true;
    }
}
