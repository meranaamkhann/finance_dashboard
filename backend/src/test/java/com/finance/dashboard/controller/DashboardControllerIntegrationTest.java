package com.finance.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.LoginRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("DashboardController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerIntegrationTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String analystToken;
    private String viewerToken;

    @BeforeEach
    void obtainTokens() throws Exception {
        adminToken   = token("admin",   "Admin@1234");
        analystToken = token("analyst", "Analyst@1234");
        viewerToken  = token("viewer",  "Viewer@1234");
    }

    @Test @Order(1)
    @DisplayName("GET /api/dashboard/summary — all roles can access (200)")
    void summary_AllRoles_200() throws Exception {
        for (String tok : new String[]{adminToken, analystToken, viewerToken}) {
            mockMvc.perform(get("/api/dashboard/summary")
                            .header("Authorization", "Bearer " + tok))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalIncome").exists())
                    .andExpect(jsonPath("$.data.totalExpenses").exists())
                    .andExpect(jsonPath("$.data.netBalance").exists())
                    .andExpect(jsonPath("$.data.healthScore").exists());
        }
    }

    @Test @Order(2)
    @DisplayName("GET /api/dashboard/health-score — analyst gets full score (200)")
    void healthScore_AsAnalyst_200() throws Exception {
        mockMvc.perform(get("/api/dashboard/health-score")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallScore").isNumber())
                .andExpect(jsonPath("$.data.grade").isString())
                .andExpect(jsonPath("$.data.insights").isArray());
    }

    @Test @Order(3)
    @DisplayName("GET /api/dashboard/health-score — viewer is forbidden (403)")
    void healthScore_AsViewer_403() throws Exception {
        mockMvc.perform(get("/api/dashboard/health-score")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    @Test @Order(4)
    @DisplayName("GET /api/dashboard/summary/range — valid range returns breakdown")
    void summaryByRange_ValidDates_200() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary/range")
                        .param("from", "2024-01-01")
                        .param("to",   "2024-12-31")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").exists())
                .andExpect(jsonPath("$.data.savingsRate").exists());
    }

    @Test @Order(5)
    @DisplayName("GET /api/dashboard/summary/range — from > to returns 400")
    void summaryByRange_InvalidDates_400() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary/range")
                        .param("from", "2024-12-31")
                        .param("to",   "2024-01-01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(6)
    @DisplayName("GET /api/dashboard/trends/monthly — returns monthly trend data")
    void monthlyTrends_200() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends/monthly")
                        .param("months", "3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(7)
    @DisplayName("GET /api/dashboard/categories — returns category breakdown")
    void categories_AsAdmin_200() throws Exception {
        mockMvc.perform(get("/api/dashboard/categories")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(8)
    @DisplayName("GET /api/dashboard/spending-by-day — returns day-of-week pattern")
    void spendingByDay_200() throws Exception {
        mockMvc.perform(get("/api/dashboard/spending-by-day")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk());
    }

    private String token(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }
}
