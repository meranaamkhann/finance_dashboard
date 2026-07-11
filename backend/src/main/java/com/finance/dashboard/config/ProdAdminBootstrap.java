package com.finance.dashboard.config;

import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j @Configuration @Profile("prod")
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ProdAdminBootstrap {
    private final UserRepository users;
    private final PasswordEncoder enc;

    @Value("${app.bootstrap-admin.username:}") private String bootstrapUsername;
    @Value("${app.bootstrap-admin.email:}")    private String bootstrapEmail;
    @Value("${app.bootstrap-admin.password:}") private String bootstrapPassword;

    @Bean
    CommandLineRunner bootstrapAdmin() {
        return args -> {
            if (users.count() > 0) {
                log.info("Bootstrap admin: users already exist, skipping.");
                return;
            }
            if (bootstrapUsername.isBlank() || bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
                log.warn("Bootstrap admin: BOOTSTRAP_ADMIN_ENABLED is true but credentials are missing — skipping.");
                return;
            }
            users.save(User.builder()
                .username(bootstrapUsername)
                .email(bootstrapEmail)
                .password(enc.encode(bootstrapPassword))
                .fullName("Admin")
                .role(Role.ADMIN)
                .active(true)
                .build());
            log.info("Bootstrap admin: created initial ADMIN account '{}'.", bootstrapUsername);
        };
    }
}