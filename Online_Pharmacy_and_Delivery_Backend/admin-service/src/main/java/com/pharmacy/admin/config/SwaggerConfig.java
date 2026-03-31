package com.pharmacy.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI configuration.
 * Access the UI at: http://localhost:8084/swagger-ui.html
 *
 * Paste your JWT token from the Identity Service login into the
 * "Authorize" button to test protected endpoints directly.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Admin & Reporting Service API")
                        .version("1.0.0")
                        .description("""
                                **Online Pharmacy — Admin Microservice (Port 8084)**

                                Manages:
                                - Medicine catalog CRUD
                                - Inventory, stock, and expiry alerts
                                - Order lifecycle and status transitions
                                - Prescription approval queue
                                - Dashboard KPIs
                                - Sales, inventory, and prescription reports

                                All endpoints require a valid **ADMIN** JWT Bearer token.
                                Get your token from the Identity Service: POST /api/auth/login
                                """)
                        .contact(new Contact()
                                .name("Pharmacy Dev Team")
                                .email("dev@pharmacy.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local Admin Service"),
                        new Server().url("http://localhost:8080/admin-service").description("Via Gateway")))
                // Tell Swagger to send the JWT header on every request
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token (without 'Bearer ' prefix)")));
    }
}
