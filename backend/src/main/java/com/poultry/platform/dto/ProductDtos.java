package com.poultry.platform.dto;

import com.poultry.platform.domain.ProductOrderStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class ProductDtos {

    public record ProductResponse(
            Long id,
            String code,
            String name,
            String description,
            BigDecimal priceHint,
            int durationDays,
            boolean active
    ) {}

    public record OrderRequest(
            @NotNull Long productId,
            Long listingId,
            String memo
    ) {}

    public record OrderDecisionRequest(
            String adminMemo
    ) {}

    public record OrderResponse(
            Long id,
            Long productId,
            String productCode,
            String productName,
            Long organizationId,
            String organizationName,
            Long listingId,
            String listingTitle,
            ProductOrderStatus status,
            String memo,
            String adminMemo,
            Instant createdAt,
            Instant decidedAt
    ) {}
}
