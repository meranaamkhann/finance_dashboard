package com.finance.dashboard.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Slf4j @Component
public class JwtUtils {
    private final Key key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtUtils(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expiration-ms}") long exp,
                    @Value("${app.jwt.refresh-expiration-ms}") long refExp) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = exp; this.refreshExpirationMs = refExp;
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder().setSubject(username).claim("role", role).claim("tokenType","access")
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
    public String generateRefreshToken(String username) {
        return Jwts.builder().setSubject(username).claim("tokenType","refresh")
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
    public String getUsernameFromToken(String token) { return parseClaims(token).getSubject(); }
    public String getTokenType(String token) { return (String) parseClaims(token).get("tokenType"); }
    public boolean validateToken(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { log.warn("JWT invalid: {}", e.getMessage()); return false; }
    }
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
