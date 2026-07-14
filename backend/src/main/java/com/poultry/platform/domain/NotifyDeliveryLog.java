package com.poultry.platform.domain;

import com.poultry.platform.notify.NotifyChannel;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notify_delivery_logs", indexes = {
        @Index(name = "idx_notify_delivery_request", columnList = "requestId"),
        @Index(name = "idx_notify_delivery_user", columnList = "userId")
})
public class NotifyDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String requestId;

    @Column(length = 80)
    private String clientId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotifyChannel channel;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 120)
    private String providerMessageId;

    @Column(length = 500)
    private String detail;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public NotifyChannel getChannel() { return channel; }
    public void setChannel(NotifyChannel channel) { this.channel = channel; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Instant getCreatedAt() { return createdAt; }
}
