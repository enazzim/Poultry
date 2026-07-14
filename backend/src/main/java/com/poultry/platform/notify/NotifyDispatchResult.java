package com.poultry.platform.notify;

import java.util.List;

public record NotifyDispatchResult(
        String requestId,
        List<NotifyChannelResult> results
) {}
