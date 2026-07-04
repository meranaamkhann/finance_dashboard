package com.finance.dashboard.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j @Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${app.rate-limit.capacity:200}") private long capacity;
    @Value("${app.rate-limit.refill-tokens:200}") private long refillTokens;
    @Value("${app.rate-limit.refill-seconds:60}") private long refillSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String ip = resolveIp(req);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(refillTokens, Duration.ofSeconds(refillSeconds)).build())
                .build());
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        res.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        if (probe.isConsumed()) { chain.doFilter(req, res); return; }
        long wait = probe.getNanosToWaitForRefill() / 1_000_000_000;
        res.setHeader("Retry-After", String.valueOf(wait));
        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getWriter(), Map.of("success", false, "message", "Rate limit exceeded. Retry after " + wait + "s."));
    }
    private String resolveIp(HttpServletRequest req) {
        String f = req.getHeader("X-Forwarded-For");
        if (f != null && !f.isBlank()) { String ip = f.split(",")[0].trim(); return ip.length() <= 45 ? ip : req.getRemoteAddr(); }
        String r = req.getHeader("X-Real-IP");
        return (r != null && !r.isBlank() && r.length() <= 45) ? r.trim() : req.getRemoteAddr();
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getRequestURI();
        return p.startsWith("/actuator/health") || p.startsWith("/swagger-ui") || p.startsWith("/v3/api-docs");
    }
}
