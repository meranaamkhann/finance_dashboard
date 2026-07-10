package com.finance.dashboard.config;
import com.finance.dashboard.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
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
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration @EnableWebSecurity @EnableMethodSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitingFilter rateLimitFilter;
    @Value("${spring.profiles.active:dev}") private String profile;

    @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(c->c.configurationSource(corsSource()))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth->{
                auth.requestMatchers("/api/auth/**","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html","/actuator/health").permitAll();
                auth.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll();
                if (isDev()) auth.requestMatchers("/h2-console/**").permitAll();
                auth.requestMatchers("/api/users/me","/api/users/me/**").authenticated();
                auth.requestMatchers("/api/users/**").hasRole("ADMIN");
                auth.requestMatchers("/api/audit/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.POST,"/api/records").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PUT,"/api/records/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.DELETE,"/api/records/**").hasRole("ADMIN");
                auth.requestMatchers("/api/records/export/csv","/api/budgets/**","/api/recurring/**",
                    "/api/dashboard/categories","/api/dashboard/trends/**","/api/dashboard/health-score",
                    "/api/dashboard/top-expenses","/api/dashboard/spending-by-day","/api/dashboard/summary/range")
                    .hasAnyRole("ANALYST","ADMIN");
                auth.anyRequest().authenticated();
            })
            .headers(h->{
                if (isDev()) h.frameOptions(f->f.sameOrigin());
                else { h.frameOptions(f->f.deny()); h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; frame-ancestors 'none'")); }
                h.referrerPolicy(r->r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                h.permissionsPolicy(p->p.policy("geolocation=(), microphone=(), camera=()"));
            })
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authenticationProvider(authProvider())
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean public DaoAuthenticationProvider authProvider() { var p=new DaoAuthenticationProvider(); p.setUserDetailsService(userDetailsService); p.setPasswordEncoder(passwordEncoder()); return p; }
    @Bean public AuthenticationManager authManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean public CorsConfigurationSource corsSource() {
        var c=new CorsConfiguration(); c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        c.setExposedHeaders(List.of("X-Rate-Limit-Remaining","Retry-After")); c.setAllowCredentials(true); c.setMaxAge(3600L);
        var s=new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**",c); return s;
    }
    private boolean isDev(){return "dev".equalsIgnoreCase(profile)||profile.contains("dev");}
}
