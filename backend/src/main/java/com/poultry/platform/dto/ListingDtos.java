package com.poultry.platform.dto;

import com.poultry.platform.domain.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class ListingDtos {

    public record CreateListingRequest(
            @NotNull Long categoryId,
            @NotNull ListingSide side,
            @NotNull String regionCode,
            @NotNull BigDecimal quantity,
            @NotNull String unit,
            BigDecimal targetPrice,
            @NotNull LogisticsType logisticsType,
            Instant expiresAt,
            String title,
            String memo,
            Map<String, Object> attributes
    ) {}

    public record ListingResponse(
            Long id,
            Long categoryId,
            String categoryCode,
            String categoryName,
            Long organizationId,
            String organizationName,
            String organizationPhone,
            ListingSide side,
            String regionCode,
            BigDecimal quantity,
            String unit,
            BigDecimal targetPrice,
            LogisticsType logisticsType,
            Instant expiresAt,
            ListingStatus status,
            ListingSource source,
            String title,
            String memo,
            Map<String, Object> attributes,
            Instant createdAt,
            Instant featuredUntil,
            boolean featured
    ) {}

    public record InterestRequest() {}

    public record InquiryRequest(
            @NotNull String message,
            String contactPhone
    ) {}

    public record InquiryResponse(
            Long id,
            Long listingId,
            String listingTitle,
            Long fromOrganizationId,
            String fromOrganizationName,
            String message,
            String contactPhone,
            InquiryStatus status,
            String replyMemo,
            Instant createdAt
    ) {}

    public record PreferenceRequest(
            @NotNull Long categoryId,
            java.util.List<String> regions,
            BigDecimal minQuantity,
            Map<String, Object> attributeFilters,
            boolean pushEnabled
    ) {}

    public record PreferenceResponse(
            Long id,
            Long categoryId,
            String categoryCode,
            String categoryName,
            java.util.List<String> regions,
            BigDecimal minQuantity,
            Map<String, Object> attributeFilters,
            boolean pushEnabled
    ) {}

    public record MesListingRequest(
            @NotNull String externalListingId,
            @NotNull String farmCode,
            @NotNull String categoryCode,
            @NotNull ListingSide side,
            Map<String, Object> attributes,
            @NotNull BigDecimal quantity,
            @NotNull String unit,
            BigDecimal targetPrice,
            @NotNull LogisticsType logisticsType,
            @NotNull String regionCode,
            Instant expiresAt,
            String title,
            String memo
    ) {}
}
