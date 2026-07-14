package com.poultry.platform.notify;

public interface NotifyChannelSender {
    NotifyChannel channel();
    NotifyChannelResult send(NotifyRequest request);
}
