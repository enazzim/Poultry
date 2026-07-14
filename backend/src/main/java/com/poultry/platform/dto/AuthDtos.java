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
            List<String> categoryCodes
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
            /** MES(EggFactory) 연동 농가 코드. 미연동이면 null */
            String farmCode,
            /** farmCode 등록 여부로 추론한 MES 옵트인 상태 */
            boolean mesLinked
    ) {}
}
