package com.finance.dashboard.security;

import com.finance.dashboard.exception.BadRequestException;
import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
    }
}