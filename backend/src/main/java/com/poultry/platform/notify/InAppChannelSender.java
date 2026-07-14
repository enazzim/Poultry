package com.poultry.platform.notify;

import com.poultry.platform.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class InAppChannelSender implements NotifyChannelSender {

    private final NotificationService notificationService;

    public InAppChannelSender(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public NotifyChannel channel() {
        return NotifyChannel.IN_APP;
    }

    @Override
    public NotifyChannelResult send(NotifyRequest request) {
        Long userId = request.recipient().userId();
        if (userId == null) {
            return NotifyChannelResult.skipped(channel(), "no userId");
        }
        try {
            notificationService.notifyUser(
                    userId,
                    request.message().title(),
                    request.message().body(),
                    request.message().type(),
                    request.message().refId()
            );
            return NotifyChannelResult.ok(channel(), "in-app-" + userId);
        } catch (Exception e) {
            return NotifyChannelResult.failed(channel(), e.getMessage());
        }
    }
}
