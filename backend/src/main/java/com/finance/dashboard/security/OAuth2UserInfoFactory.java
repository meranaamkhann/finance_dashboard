package com.finance.dashboard.security;

import com.finance.dashboard.exception.BadRequestException;
import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GithubOAuth2UserInfo(attributes);
            default -> throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
        };
    }
}