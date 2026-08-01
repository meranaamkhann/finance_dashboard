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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(req);
        String provider = req.getClientRegistration().getRegistrationId();
        OAuth2UserInfo info = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        if (info.getEmail() == null || info.getEmail().isBlank()) {
            log.warn("Email not returned by OAuth2 provider: {}", provider);
            throw new OAuth2AuthenticationException("Email not provided by " + provider);
        }

        Optional<User> existing = userRepository.findByEmailAndDeletedFalse(info.getEmail().toLowerCase());
        User user;

        if (existing.isPresent()) {
            user = existing.get();
            if (user.getProvider() == null) {
                user.setProvider(provider);
                user.setProviderId(info.getId());
            }
            if (info.getImageUrl() != null) user.setAvatarUrl(info.getImageUrl());
            user.setEmailVerified(true);
            userRepository.save(user);
        } else {
            String username = generateUsername(info.getEmail(), info.getName());
            user = User.builder()
                    .username(username)
                    .email(info.getEmail().toLowerCase())
                    .fullName(info.getName() != null ? info.getName() : username)
                    .password("")
                    .role(Role.ANALYST)
                    .provider(provider)
                    .providerId(info.getId())
                    .avatarUrl(info.getImageUrl())
                    .emailVerified(true)
                    .build();
            userRepository.save(user);
            log.info("New OAuth2 user created: {} via {}", username, provider);
        }

        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }

    private String generateUsername(String email, String name) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (base.length() < 3) base = "user" + base;
        String candidate = base;
        int i = 1;
        while (userRepository.existsByUsernameAndDeletedFalse(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }
}