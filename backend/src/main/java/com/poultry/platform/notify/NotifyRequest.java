package com.poultry.platform.notify;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Dispatch request used by platform matching today and by future company Notify API.
 */
public record NotifyRequest(
        String requestId,
        String clientId,
        NotifyRecipient recipient,
        NotifyMessage message,
        Set<NotifyChannel> channels
) {
    public static NotifyRequest of(NotifyRecipient recipient, NotifyMessage message, Set<NotifyChannel> channels) {
        return new NotifyRequest(UUID.randomUUID().toString(), "poultry-platform", recipient, message, channels);
    }

    public static NotifyRequest interestMatch(NotifyRecipient recipient, NotifyMessage message,
                                              boolean sms, boolean alimtalk) {
        EnumSet<NotifyChannel> channels = EnumSet.of(NotifyChannel.IN_APP);
        if (sms && recipient.smsConsent()) {
            channels.add(NotifyChannel.SMS);
        }
        if (alimtalk && recipient.alimtalkConsent()) {
            channels.add(NotifyChannel.ALIMTALK);
        }
        return of(recipient, message, channels);
    }
}
