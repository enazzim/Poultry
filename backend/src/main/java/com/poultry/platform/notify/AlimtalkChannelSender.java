package com.poultry.platform.notify;

import org.springframework.stereotype.Component;

@Component
public class AlimtalkChannelSender implements NotifyChannelSender {

    private final SolapiClient solapiClient;

    public AlimtalkChannelSender(SolapiClient solapiClient) {
        this.solapiClient = solapiClient;
    }

    @Override
    public NotifyChannel channel() {
        return NotifyChannel.ALIMTALK;
    }

    @Override
    public NotifyChannelResult send(NotifyRequest request) {
        if (!request.recipient().alimtalkConsent()) {
            return NotifyChannelResult.skipped(channel(), "no alimtalkConsent");
        }
        String phone = request.recipient().phone();
        if (phone == null || phone.isBlank()) {
            return NotifyChannelResult.skipped(channel(), "no phone");
        }
        try {
            String text = request.message().title() + "\n" + request.message().body();
            String providerId = solapiClient.sendAlimtalk(
                    phone,
                    text,
                    request.message().templateVariables()
            );
            return NotifyChannelResult.ok(channel(), providerId);
        } catch (Exception e) {
            return NotifyChannelResult.failed(channel(), e.getMessage());
        }
    }
}
