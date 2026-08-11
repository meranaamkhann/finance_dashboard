package com.finance.dashboard.security;

import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(req);
        } catch (Exception e) {
            log.error("Failed to load user from Google: {}", e.getMessage());
            throw new OAuth2AuthenticationException("Failed to fetch user info from Google");
        }

        try {
            Map<String, Object> attrs = oAuth2User.getAttributes();
            String email = (String) attrs.get("email");
            String name  = (String) attrs.get("name");
            String sub   = (String) attrs.get("sub");
            String pic   = (String) attrs.get("picture");

            if (email == null || email.isBlank()) {
                throw new OAuth2AuthenticationException("Email not provided by Google");
            }

            email = email.toLowerCase().trim();
            Optional<User> existing = userRepository.findByEmailAndDeletedFalse(email);
            User user;

            if (existing.isPresent()) {
                user = existing.get();
                if (name != null) user.setFullName(name);
                try { user.setEmailVerified(true); } catch (Exception ignored) {}
                try { if (sub != null) user.setProviderId(sub); } catch (Exception ignored) {}
                try { if (pic != null) user.setAvatarUrl(pic); } catch (Exception ignored) {}
                try { user.setProvider("google"); } catch (Exception ignored) {}
                userRepository.save(user);
                log.info("OAuth2 existing user logged in: {}", user.getUsername());
            } else {
                String username = generateUsername(email, name);
                user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setFullName(name != null ? name : username);
                user.setPassword(UUID.randomUUID().toString());
                user.setRole(Role.ANALYST);
                user.setActive(true);
                user.setDeleted(false);
                try { user.setProvider("google"); } catch (Exception ignored) {}
                try { user.setProviderId(sub); } catch (Exception ignored) {}
                try { user.setAvatarUrl(pic); } catch (Exception ignored) {}
                try { user.setEmailVerified(true); } catch (Exception ignored) {}
                userRepository.save(user);
                log.info("OAuth2 new user created: {}", username);
            }

            return new OAuth2UserPrincipal(user, attrs);

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 user processing error: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Login failed. Please try again.");
        }
    }

    private String generateUsername(String email, String name) {
        String base = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase();
        if (base.length() < 3) base = "user" + base;
        if (base.length() > 20) base = base.substring(0, 20);
        String candidate = base;
        int i = 1;
        while (userRepository.existsByUsernameAndDeletedFalse(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }
}