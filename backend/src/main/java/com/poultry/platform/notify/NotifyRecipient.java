package com.poultry.platform.notify;

/**
 * Destination resolved from consent + contact. Future API tenants will pass this explicitly.
 */
public record NotifyRecipient(
        Long userId,
        String displayName,
        String phone,
        boolean smsConsent,
        boolean alimtalkConsent,
        boolean inApp
) {}
