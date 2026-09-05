package com.stockcheck.backend.security;

import com.stockcheck.backend.role.RoleName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<StockcheckPrincipal> getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof StockcheckPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentPrincipal().map(StockcheckPrincipal::getUserId);
    }

    public static Optional<UUID> getCurrentTenantId() {
        return getCurrentPrincipal().map(StockcheckPrincipal::getTenantId);
    }

    public static boolean hasRole(RoleName role) {
        String roleAuthority = "ROLE_" + role.name();
        return getCurrentPrincipal()
                .map(principal -> principal.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(roleAuthority)))
                .orElse(false);
    }
}
