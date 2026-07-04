package com.finance.dashboard.config;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;
@Configuration
@SecurityScheme(name="bearerAuth",type=SecuritySchemeType.HTTP,scheme="bearer",bearerFormat="JWT")
public class OpenApiConfig {
    @Bean public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Finance Dashboard API").version("2.0.0")
            .description("Production-grade Finance Dashboard — JWT auth, RBAC, budgets, health score, audit trail, CSV export")
            .contact(new Contact().name("Asad Khan").url("https://github.com/meranaamkhann"))
            .license(new License().name("MIT")));
    }
}
