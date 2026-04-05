package com.finance.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.CreateRecordRequest;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.TransactionType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("FinancialRecordController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialRecordControllerIntegrationTest {

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

    // ── GET /api/records ──────────────────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("GET /api/records — viewer can list (200)")
    void list_AsViewer_200() throws Exception {
        mockMvc.perform(get("/api/records").header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThan(0)));
    }

    @Test @Order(2)
    @DisplayName("GET /api/records — no auth → 403")
    void list_NoAuth_403() throws Exception {
        mockMvc.perform(get("/api/records"))
                .andExpect(status().isForbidden());
    }

    @Test @Order(3)
    @DisplayName("GET /api/records?type=INCOME — only income records returned")
    void list_FilterByType_OnlyIncome() throws Exception {
        mockMvc.perform(get("/api/records")
                        .param("type", "INCOME")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].type", everyItem(is("INCOME"))));
    }

    @Test @Order(4)
    @DisplayName("GET /api/records — keyword search filters by description")
    void list_KeywordSearch_FiltersResults() throws Exception {
        mockMvc.perform(get("/api/records")
                        .param("keyword", "salary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ── POST /api/records ─────────────────────────────────────────────────────

    @Test @Order(5)
    @DisplayName("POST /api/records — admin creates record (201)")
    void create_AsAdmin_201() throws Exception {
        CreateRecordRequest req = buildRequest("75000", TransactionType.INCOME,
                Category.SALARY, LocalDate.now().minusDays(1), "Integration test income");

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(75000.0))
                .andExpect(jsonPath("$.data.type").value("INCOME"))
                .andExpect(jsonPath("$.data.createdByUsername").value("admin"));
    }

    @Test @Order(6)
    @DisplayName("POST /api/records — viewer forbidden (403)")
    void create_AsViewer_403() throws Exception {
        CreateRecordRequest req = buildRequest("100", TransactionType.EXPENSE,
                Category.FOOD, LocalDate.now().minusDays(1), "Viewer tries to create");

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test @Order(7)
    @DisplayName("POST /api/records — future date fails validation (400)")
    void create_FutureDate_400() throws Exception {
        CreateRecordRequest req = buildRequest("500", TransactionType.EXPENSE,
                Category.FOOD, LocalDate.now().plusDays(5), "Future date");

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.date").exists());
    }

    @Test @Order(8)
    @DisplayName("POST /api/records — negative amount fails validation (400)")
    void create_NegativeAmount_400() throws Exception {
        CreateRecordRequest req = buildRequest("-100", TransactionType.EXPENSE,
                Category.FOOD, LocalDate.now().minusDays(1), "Negative");

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    // ── PUT /api/records/{id} ─────────────────────────────────────────────────

    @Test @Order(9)
    @DisplayName("PUT /api/records/{id} — analyst cannot update (403)")
    void update_AsAnalyst_403() throws Exception {
        mockMvc.perform(put("/api/records/1")
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"updated\"}"))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/records/export/csv ───────────────────────────────────────────

    @Test @Order(10)
    @DisplayName("GET /api/records/export/csv — admin gets CSV download")
    void export_AsAdmin_ReturnsCsv() throws Exception {
        mockMvc.perform(get("/api/records/export/csv")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment; filename=\"finance-export-")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test @Order(11)
    @DisplayName("GET /api/records/export/csv — viewer forbidden (403)")
    void export_AsViewer_403() throws Exception {
        mockMvc.perform(get("/api/records/export/csv")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateRecordRequest buildRequest(String amount, TransactionType type,
                                              Category category, LocalDate date, String desc) {
        CreateRecordRequest req = new CreateRecordRequest();
        req.setAmount(new BigDecimal(amount));
        req.setType(type);
        req.setCategory(category);
        req.setDate(date);
        req.setDescription(desc);
        return req;
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
