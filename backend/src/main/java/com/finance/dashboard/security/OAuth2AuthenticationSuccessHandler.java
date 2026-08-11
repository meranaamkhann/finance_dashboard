package com.finance.dashboard.security;

import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
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

    @Value("${app.frontend.url:https://finance-pro-sibbus.vercel.app}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        try {
            User user = null;

            Object principal = authentication.getPrincipal();

            /*
             * CustomOAuth2UserService returns OAuth2UserPrincipal
             * containing the actual database User.
             */
            if (principal instanceof OAuth2UserPrincipal oauthPrincipal) {
                user = oauthPrincipal.getUser();
            }

            /*
             * Fallback: resolve the user from Google's email.
             */
            if (user == null && principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");

                if (email != null && !email.isBlank()) {
                    email = email.toLowerCase().trim();

                    user = userRepository
                            .findByEmailAndDeletedFalse(email)
                            .orElse(null);
                }
            }

            if (user == null) {
                log.error("OAuth2 user could not be resolved from authenticated principal");

                response.sendRedirect(
                        frontendUrl + "/login?error=oauth2_user_not_found"
                );
                return;
            }

            if (!user.isActive() || user.isDeleted()) {
                log.warn(
                        "OAuth2 login rejected for inactive/deleted user: {}",
                        user.getUsername()
                );

                response.sendRedirect(
                        frontendUrl + "/login?error=account_disabled"
                );
                return;
            }

            String role = user.getRole().name();

            String accessToken =
                    jwtUtils.generateAccessToken(
                            user.getUsername(),
                            role
                    );

            String refreshToken =
                    jwtUtils.generateRefreshToken(
                            user.getUsername()
                    );

            String redirectUrl =
                    frontendUrl
                            + "/oauth2/callback"
                            + "?accessToken="
                            + URLEncoder.encode(
                                    accessToken,
                                    StandardCharsets.UTF_8
                            )
                            + "&refreshToken="
                            + URLEncoder.encode(
                                    refreshToken,
                                    StandardCharsets.UTF_8
                            )
                            + "&username="
                            + URLEncoder.encode(
                                    user.getUsername(),
                                    StandardCharsets.UTF_8
                            )
                            + "&role="
                            + URLEncoder.encode(
                                    role,
                                    StandardCharsets.UTF_8
                            );

            log.info(
                    "OAuth2 login successful for user: {}",
                    user.getUsername()
            );

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 success handler failed", e);

            response.sendRedirect(
                    frontendUrl + "/login?error=oauth2_login_failed"
            );
        }
    }
}