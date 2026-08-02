package com.finance.dashboard.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test void contextLoads() {}

    @Test void actuatorHealth_returnsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test void unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/records")).andExpect(status().isUnauthorized());
    }

    @Test void login_admin_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin"); req.setPassword("Admin@1234");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.success").value(true))
           .andExpect(jsonPath("$.data.accessToken").exists())
           .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin"); req.setPassword("wrong");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isUnauthorized());
    }

    @Test void login_blankBody_returns422() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isUnprocessableEntity())
           .andExpect(jsonPath("$.errors").exists());
    }

    @Test void swaggerUi_redirects() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }
}
