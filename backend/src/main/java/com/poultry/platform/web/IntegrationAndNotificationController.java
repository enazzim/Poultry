package com.poultry.platform.web;

import com.poultry.platform.dto.ListingDtos;
import com.poultry.platform.security.SecurityUtils;
import com.poultry.platform.service.ListingService;
import com.poultry.platform.service.NotificationService;
import com.poultry.platform.domain.NotificationLog;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IntegrationAndNotificationController {

    private final ListingService listingService;
    private final NotificationService notificationService;
    private final String mesApiKey;

    public IntegrationAndNotificationController(
            ListingService listingService,
            NotificationService notificationService,
            @Value("${app.mes.api-key}") String mesApiKey) {
        this.listingService = listingService;
        this.notificationService = notificationService;
        this.mesApiKey = mesApiKey;
    }

    @PostMapping("/integrations/mes/listings")
    public ListingDtos.ListingResponse mesListing(
            @RequestHeader(value = "X-MES-API-KEY", required = false) String apiKey,
            @Valid @RequestBody ListingDtos.MesListingRequest request) {
        if (apiKey == null || !apiKey.equals(mesApiKey)) {
            throw new IllegalArgumentException("MES API 키가 유효하지 않습니다.");
        }
        return listingService.createFromMes(request);
    }

    @GetMapping("/notifications")
    public List<Map<String, Object>> notifications() {
        Long userId = SecurityUtils.currentUser().getId();
        return notificationService.myNotifications(userId).stream().map(this::toMap).toList();
    }

    @GetMapping("/notifications/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.unreadCount(SecurityUtils.currentUser().getId()));
    }

    @PostMapping("/notifications/{id}/read")
    public void markRead(@PathVariable Long id) {
        notificationService.markRead(SecurityUtils.currentUser().getId(), id);
    }

    private Map<String, Object> toMap(NotificationLog n) {
        return Map.of(
                "id", n.getId(),
                "title", n.getTitle(),
                "body", n.getBody(),
                "type", n.getType() != null ? n.getType() : "",
                "refId", n.getRefId() != null ? n.getRefId() : 0L,
                "readFlag", n.isReadFlag(),
                "createdAt", n.getCreatedAt().toString()
        );
    }
}
