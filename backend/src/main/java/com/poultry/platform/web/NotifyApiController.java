package com.poultry.platform.web;

import com.poultry.platform.notify.*;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * Seed endpoint for the future company Notify product API.
 * Path prefix /api/notify/v1 is reserved for multi-tenant API keys later.
 */
@RestController
@RequestMapping("/api/notify/v1")
@ConditionalOnProperty(prefix = "app.notify", name = "api-enabled", havingValue = "true", matchIfMissing = true)
public class NotifyApiController {

    private final NotifyPort notifyPort;

    public NotifyApiController(NotifyPort notifyPort) {
        this.notifyPort = notifyPort;
    }

    @PostMapping("/messages")
    public NotifyApiDtos.SendResponse send(@Valid @RequestBody NotifyApiDtos.SendRequest request) {
        NotifyRecipient recipient = new NotifyRecipient(
                request.userId(),
                request.displayName(),
                request.phone(),
                request.smsConsent(),
                request.alimtalkConsent(),
                request.inApp()
        );
        NotifyMessage message = new NotifyMessage(
                request.title(),
                request.body(),
                request.type() != null ? request.type() : "CUSTOM",
                request.refId(),
                request.templateVariables() != null ? request.templateVariables() : java.util.Map.of()
        );
        NotifyRequest notifyRequest = new NotifyRequest(
                java.util.UUID.randomUUID().toString(),
                "notify-api-v1",
                recipient,
                message,
                request.channels()
        );
        NotifyDispatchResult result = notifyPort.dispatch(notifyRequest);
        return new NotifyApiDtos.SendResponse(result.requestId(), result.results());
    }
}
