package com.poultry.platform.repository;

import com.poultry.platform.domain.NotifyDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotifyDeliveryLogRepository extends JpaRepository<NotifyDeliveryLog, Long> {
    List<NotifyDeliveryLog> findByRequestIdOrderByCreatedAtAsc(String requestId);
}
