package com.finance.dashboard.security;

import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:https://finance-pro-sibbus.vercel.app}")
    private String frontendUrl;

@Override
public void onAuthenticationSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication auth) throws IOException {
    try {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) auth.getPrincipal();
        User user = principal.getUser();

        String role        = "ROLE_" + user.getRole().name();
        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), role);
        String rawRefresh  = jwtUtils.generateRefreshToken(user.getUsername());

        try {
            refreshTokenService.create(user, rawRefresh);
        } catch (Exception e) {
            log.warn("Refresh token store failed: {}", e.getMessage());
        }

        String redirectUrl = frontendUrl + "/oauth2/callback"
                + "?token="   + accessToken
                + "&refresh=" + rawRefresh;

        response.sendRedirect(redirectUrl);

    } catch (Exception e) {
        log.error("OAuth2 success handler error: {}", e.getMessage(), e);
        response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
    }
}
}