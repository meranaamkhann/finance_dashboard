package com.finance.dashboard.security;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
class JwtUtilsTest {
    private JwtUtils jwtUtils;
    private static final String SECRET = "3f7d9a1b2c4e6f8a0b2d4f6a8c0e2f4a6b8c0d2e4f6a8b0c2d4e6f8a0b2c4e6f";
    @BeforeEach void setUp() { jwtUtils = new JwtUtils(SECRET, 86_400_000L, 604_800_000L); }
    @Test void access_token_validates() { String t=jwtUtils.generateAccessToken("admin","ROLE_ADMIN"); assertThat(jwtUtils.validateToken(t)).isTrue(); assertThat(jwtUtils.getTokenType(t)).isEqualTo("access"); }
    @Test void refresh_token_correct_type() { assertThat(jwtUtils.getTokenType(jwtUtils.generateRefreshToken("admin"))).isEqualTo("refresh"); }
    @Test void tampered_token_invalid() { assertThat(jwtUtils.validateToken(jwtUtils.generateAccessToken("a","r")+"x")).isFalse(); }
    @Test void expired_token_invalid() throws Exception { JwtUtils s=new JwtUtils(SECRET,1L,1L); String t=s.generateAccessToken("a","r"); Thread.sleep(10); assertThat(s.validateToken(t)).isFalse(); }
}
