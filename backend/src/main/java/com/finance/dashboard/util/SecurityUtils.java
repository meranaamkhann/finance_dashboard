package com.finance.dashboard.util;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;
    public UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("No authenticated user");
        return (UserDetailsImpl) auth.getPrincipal();
    }
    public Long getCurrentUserId() { return getCurrentUserDetails().getId(); }
    public String getCurrentUsername() { return getCurrentUserDetails().getUsername(); }
    public User getCurrentUser() {
        return userRepository.findByUsernameAndDeletedFalse(getCurrentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
