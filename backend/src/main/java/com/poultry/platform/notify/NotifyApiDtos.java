package com.poultry.platform.notify;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

/**
 * DTOs for the company Notify API surface (v1).
 * Today authenticated members can trial-send; later replaced by tenant API keys.
 */
public class NotifyApiDtos {

    public record SendRequest(
            @NotBlank String phone,
            String displayName,
            boolean smsConsent,
            boolean alimtalkConsent,
            boolean inApp,
            Long userId,
            @NotBlank String title,
            @NotBlank String body,
            String type,
            Long refId,
            Map<String, String> templateVariables,
            @NotEmpty Set<NotifyChannel> channels
    ) {}

    public record SendResponse(
            String requestId,
            java.util.List<NotifyChannelResult> results
    ) {}
}
