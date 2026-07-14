package com.poultry.platform.notify;

import com.poultry.platform.domain.NotifyDeliveryLog;
import com.poultry.platform.repository.NotifyDeliveryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Default NotifyPort — channel fan-out + delivery audit log.
 * Future SaaS Notify API will call this (or a multi-tenant wrapper around it).
 */
@Service
public class NotificationDispatchService implements NotifyPort {

    private final Map<NotifyChannel, NotifyChannelSender> senders;
    private final NotifyDeliveryLogRepository deliveryLogRepository;

    public NotificationDispatchService(List<NotifyChannelSender> senderList,
                                       NotifyDeliveryLogRepository deliveryLogRepository) {
        this.senders = new EnumMap<>(NotifyChannel.class);
        for (NotifyChannelSender sender : senderList) {
            this.senders.put(sender.channel(), sender);
        }
        this.deliveryLogRepository = deliveryLogRepository;
    }

    @Override
    @Transactional
    public NotifyDispatchResult dispatch(NotifyRequest request) {
        List<NotifyChannelResult> results = new ArrayList<>();
        for (NotifyChannel channel : request.channels()) {
            NotifyChannelSender sender = senders.get(channel);
            NotifyChannelResult result = sender == null
                    ? NotifyChannelResult.failed(channel, "sender not registered")
                    : sender.send(request);
            results.add(result);
            saveLog(request, result);
        }
        return new NotifyDispatchResult(request.requestId(), results);
    }

    private void saveLog(NotifyRequest request, NotifyChannelResult result) {
        NotifyDeliveryLog log = new NotifyDeliveryLog();
        log.setRequestId(request.requestId());
        log.setClientId(request.clientId());
        log.setUserId(request.recipient().userId());
        log.setChannel(result.channel());
        log.setSuccess(result.success());
        log.setProviderMessageId(result.providerMessageId());
        log.setDetail(result.detail());
        deliveryLogRepository.save(log);
    }
}
