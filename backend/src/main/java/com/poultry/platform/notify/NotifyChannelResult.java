package com.poultry.platform.notify;

public record NotifyChannelResult(
        NotifyChannel channel,
        boolean success,
        String providerMessageId,
        String detail
) {
    public static NotifyChannelResult ok(NotifyChannel channel, String providerMessageId) {
        return new NotifyChannelResult(channel, true, providerMessageId, "OK");
    }

    public static NotifyChannelResult skipped(NotifyChannel channel, String reason) {
        return new NotifyChannelResult(channel, false, null, "SKIPPED: " + reason);
    }

    public static NotifyChannelResult failed(NotifyChannel channel, String detail) {
        return new NotifyChannelResult(channel, false, null, "FAILED: " + detail);
    }
}
