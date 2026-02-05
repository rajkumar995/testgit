package com.medidropbox.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for MediDropBox
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI mediDropBoxOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediDropBox API")
                        .description("Hospital Booking, Queue, RBAC & User Management System API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MediDropBox Team")
                                .email("support@medidropbox.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.medidropbox.com").description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authentication")));
    }
}
