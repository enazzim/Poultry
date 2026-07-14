package com.poultry.platform.notify;

import java.util.Map;

/**
 * Channel-agnostic message payload. Future public Notify API will accept the same shape.
 */
public record NotifyMessage(
        String title,
        String body,
        String type,
        Long refId,
        /** Alimtalk / template variables (e.g. category, title) */
        Map<String, String> templateVariables
) {
    public NotifyMessage(String title, String body, String type, Long refId) {
        this(title, body, type, refId, Map.of());
    }
}
