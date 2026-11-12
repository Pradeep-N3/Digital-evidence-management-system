package com.decms.domain.model.user;

import com.decms.domain.interfaces.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true, nullable = false, length = 50)
    protected String username;

    @Column(unique = true, nullable = false, length = 100)
    protected String email;

    @JsonIgnore
    @Column(nullable = false)
    protected String passwordHash;

    @Column(nullable = false, length = 100)
    protected String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    protected Role role;

    @Column(nullable = false)
    protected boolean active = true;

    @Column(length = 50)
    protected String badgeNumber;

    @Column(length = 100)
    protected String department;

    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String email, String passwordHash, String fullName, Role role, String badgeNumber, String department) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.badgeNumber = badgeNumber;
        this.department = department;
    }

    // Abstract method embodying Polymorphism
    public abstract boolean hasPermission(String permission);

    @Override
    public String generateAuditSnapshot() {
        return String.format("User[id=%d, username='%s', role=%s, active=%b]", id, username, role, active);
    }

    @Override
    public String getAuditIdentifier() {
        return "USER-" + username;
    }

    // Getters and Setters (Encapsulation)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
