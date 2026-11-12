package com.decms.domain.model.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Administrator extends User {

    public Administrator() {
        super();
        this.role = Role.ROLE_ADMIN;
    }

    public Administrator(String username, String email, String passwordHash, String fullName, String badgeNumber, String department) {
        super(username, email, passwordHash, fullName, Role.ROLE_ADMIN, badgeNumber, department);
    }

    @Override
    public boolean hasPermission(String permission) {
        // Admin has universal administrative capabilities
        return true;
    }
}
