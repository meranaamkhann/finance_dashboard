package com.finance.dashboard.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev")
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Test void contextLoads() {}
    @Test void actuatorHealth() throws Exception { mvc.perform(get("/actuator/health")).andExpect(status().isOk()); }
    @Test void unauthenticated_401() throws Exception { mvc.perform(get("/api/records")).andExpect(status().isUnauthorized()); }
    @Test void login_admin_200() throws Exception {
        LoginRequest r=new LoginRequest(); r.setUsername("admin"); r.setPassword("Admin@1234");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(r)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.accessToken").exists());
    }
    @Test void login_wrong_401() throws Exception {
        LoginRequest r=new LoginRequest(); r.setUsername("admin"); r.setPassword("wrong");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(r)))
                .andExpect(status().isUnauthorized());
    }
}
