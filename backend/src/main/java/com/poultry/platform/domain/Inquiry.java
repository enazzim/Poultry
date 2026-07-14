package com.poultry.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_organization_id", nullable = false)
    private Organization fromOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private AppUser fromUser;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 40)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.NEW;

    @Column(length = 1000)
    private String replyMemo;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant repliedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public Organization getFromOrganization() { return fromOrganization; }
    public void setFromOrganization(Organization fromOrganization) { this.fromOrganization = fromOrganization; }
    public AppUser getFromUser() { return fromUser; }
    public void setFromUser(AppUser fromUser) { this.fromUser = fromUser; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public InquiryStatus getStatus() { return status; }
    public void setStatus(InquiryStatus status) { this.status = status; }
    public String getReplyMemo() { return replyMemo; }
    public void setReplyMemo(String replyMemo) { this.replyMemo = replyMemo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRepliedAt() { return repliedAt; }
    public void setRepliedAt(Instant repliedAt) { this.repliedAt = repliedAt; }
}
