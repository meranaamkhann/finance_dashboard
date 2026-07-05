package com.finance.dashboard.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Override BCrypt strength to 4 in tests (vs 12 in prod).
 * This makes test seeding and login verification ~100x faster.
 */
@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }
}
