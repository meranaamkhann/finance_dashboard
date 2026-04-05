package com.finance.dashboard.util;

import com.finance.dashboard.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static String currentUsername() {
        return currentUser().getUsername();
    }
}
