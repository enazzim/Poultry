package com.poultry.platform.notify;

import org.springframework.stereotype.Component;

@Component
public class SmsChannelSender implements NotifyChannelSender {

    private final SolapiClient solapiClient;

    public SmsChannelSender(SolapiClient solapiClient) {
        this.solapiClient = solapiClient;
    }

    @Override
    public NotifyChannel channel() {
        return NotifyChannel.SMS;
    }

    @Override
    public NotifyChannelResult send(NotifyRequest request) {
        if (!request.recipient().smsConsent()) {
            return NotifyChannelResult.skipped(channel(), "no smsConsent");
        }
        String phone = request.recipient().phone();
        if (phone == null || phone.isBlank()) {
            return NotifyChannelResult.skipped(channel(), "no phone");
        }
        try {
            String text = request.message().title() + "\n" + request.message().body();
            String providerId = solapiClient.sendSms(phone, text);
            return NotifyChannelResult.ok(channel(), providerId);
        } catch (Exception e) {
            return NotifyChannelResult.failed(channel(), e.getMessage());
        }
    }
}
