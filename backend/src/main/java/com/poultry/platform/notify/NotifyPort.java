package com.poultry.platform.notify;

/**
 * Company-facing notify port.
 * <p>
 * Today's platform matching calls this locally. Later the same contract can be exposed as
 * a multi-tenant HTTP Notify API (API key / OAuth) without rewriting channel logic.
 */
public interface NotifyPort {
    NotifyDispatchResult dispatch(NotifyRequest request);
}
