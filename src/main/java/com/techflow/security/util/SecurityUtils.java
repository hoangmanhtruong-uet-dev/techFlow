package com.techflow.security.util;

import com.techflow.common.exception.UnauthorizedAccessException;
import com.techflow.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUserDetails().map(CustomUserDetails::getId);
    }

    public static UUID getRequiredCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedAccessException("Current user is not authenticated"));
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserDetails().map(CustomUserDetails::getEmail);
    }

    public static boolean hasRole(String role) {
        return getCurrentUserDetails()
                .map(userDetails -> userDetails.getRole() != null && userDetails.getRole().name().equalsIgnoreCase(role))
                .orElse(false);
    }

    public static boolean isCurrentUserAdmin() {
        return hasRole("ADMIN");
    }
}