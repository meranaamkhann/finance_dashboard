package com.finance.dashboard.config;
import com.finance.dashboard.security.JwtAuthenticationFilter;
import com.finance.dashboard.security.RateLimitingFilter;
import com.finance.dashboard.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitingFilter rateLimitFilter;

    @Value("${spring.profiles.active:dev}") private String profile;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(corsSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
    .authenticationEntryPoint((request, response, authException) -> {
        response.sendError(
            jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
            authException.getMessage()
        );
    })
)
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                    "/api/auth/**",
                    "/api/plans/**",
                    "/api/landing/**",
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll();
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                if ("dev".equalsIgnoreCase(profile))
                    auth.requestMatchers("/h2-console/**").permitAll();
                auth.requestMatchers("/actuator/**").hasRole("ADMIN");
                auth.requestMatchers("/api/users/me", "/api/users/me/**").authenticated();
                auth.requestMatchers("/api/users/**").hasRole("ADMIN");
                auth.requestMatchers("/api/audit/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.POST,   "/api/records").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PUT,    "/api/records/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/api/records/**").hasRole("ADMIN");
                auth.requestMatchers(
                    "/api/records/export/csv", "/api/budgets/**", "/api/recurring/**",
                    "/api/dashboard/categories", "/api/dashboard/trends/**",
                    "/api/dashboard/health-score", "/api/dashboard/top-expenses",
                    "/api/dashboard/spending-by-day", "/api/dashboard/summary/range"
                ).hasAnyRole("ANALYST", "ADMIN");
                auth.anyRequest().authenticated();
            })
            .headers(h -> {
                if ("dev".equalsIgnoreCase(profile))
                    h.frameOptions(f -> f.sameOrigin());
                else {
                    h.frameOptions(f -> f.deny());
                    h.contentSecurityPolicy(c -> c.policyDirectives(
                        "default-src 'self'; frame-ancestors 'none'"));
                }
                h.referrerPolicy(r -> r.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
            })
            .authenticationProvider(authProvider())
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public DaoAuthenticationProvider authProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }
    @Bean public AuthenticationManager authManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean public CorsConfigurationSource corsSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        cfg.setExposedHeaders(List.of("X-Rate-Limit-Remaining","Retry-After"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
