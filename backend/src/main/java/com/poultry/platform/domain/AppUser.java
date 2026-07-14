package com.poultry.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    /** Phone used for SMS / Alimtalk (consent required to send). */
    @Column(length = 40)
    private String notifyPhone;

    @Column(nullable = false)
    private boolean smsConsent = false;

    @Column(nullable = false)
    private boolean alimtalkConsent = false;

    private Instant consentAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public String getNotifyPhone() { return notifyPhone; }
    public void setNotifyPhone(String notifyPhone) { this.notifyPhone = notifyPhone; }
    public boolean isSmsConsent() { return smsConsent; }
    public void setSmsConsent(boolean smsConsent) { this.smsConsent = smsConsent; }
    public boolean isAlimtalkConsent() { return alimtalkConsent; }
    public void setAlimtalkConsent(boolean alimtalkConsent) { this.alimtalkConsent = alimtalkConsent; }
    public Instant getConsentAt() { return consentAt; }
    public void setConsentAt(Instant consentAt) { this.consentAt = consentAt; }
    public Instant getCreatedAt() { return createdAt; }
}
