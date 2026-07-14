package com.poultry.platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static UserPrincipal currentUser() {
        UserPrincipal principal = currentUserOrNull();
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return principal;
    }

    /** Guest browse: null when not authenticated */
    public static UserPrincipal currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
