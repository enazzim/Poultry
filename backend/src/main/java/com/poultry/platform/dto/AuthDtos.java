package com.poultry.platform.dto;

import com.poultry.platform.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String displayName,
            @NotNull UserRole role,
            @NotBlank String organizationName,
            String regionCode,
            String phone,
            String farmCode,
            /** Partner trading categories (PARTNER required). */
            List<String> categoryCodes,
            /** Categories to watch for new listings (required for all roles). */
            @NotNull List<String> interestCategoryCodes,
            boolean smsConsent,
            boolean alimtalkConsent
    ) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record AuthResponse(
            String token,
            Long userId,
            String username,
            String displayName,
            UserRole role,
            Long organizationId,
            String organizationName,
            String farmCode,
            boolean mesLinked,
            boolean smsConsent,
            boolean alimtalkConsent
    ) {}
}
