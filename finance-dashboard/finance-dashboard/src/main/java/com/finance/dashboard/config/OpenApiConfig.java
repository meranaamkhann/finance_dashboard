package com.finance.dashboard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Dashboard API")
                        .version("1.0.0")
                        .description("""
                            ## Finance Data Processing & Access Control Backend
                            
                            A professional-grade REST API for managing personal and organisational finances.
                            
                            ### Features
                            - **JWT Authentication** with role-based access control (VIEWER / ANALYST / ADMIN)
                            - **Financial Records** — create, read, update, soft-delete with rich filtering
                            - **Budget Management** — set category budgets with real-time usage & alert status
                            - **Recurring Transactions** — define rules; the scheduler auto-posts entries daily
                            - **Financial Health Score** — composite 0–100 score derived from 5 signals
                            - **Audit Trail** — every mutation is logged with actor, IP, before/after state
                            - **In-app Notifications** — budget alerts and recurring-execution confirmations
                            - **CSV Export** — download any filtered record set as a spreadsheet
                            - **Rate Limiting** — per-IP token bucket (100 req / 60 s)
                            
                            ### Quick Start
                            1. `POST /api/auth/login` with `admin / Admin@1234`
                            2. Copy the `token` from the response
                            3. Click **Authorize** above and paste `Bearer <token>`
                            4. Explore the endpoints
                            """)
                        .contact(new Contact()
                                .name("Finance Dashboard")
                                .email("admin@finance.dev")
                                .url("https://github.com/meranaamkhann"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local dev")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components()
                        .addSecuritySchemes(scheme, new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
