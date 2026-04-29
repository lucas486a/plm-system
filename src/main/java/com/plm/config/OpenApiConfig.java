package com.plm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PLM System API")
                        .description("Product Lifecycle Management System REST API. "
                                + "Manages parts, documents, BOMs, engineering change requests/orders, "
                                + "assemblies, users, roles, and audit logs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PLM Team")
                                .email("plm@example.com"))
                        .license(new License()
                                .name("Internal Use Only")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development")));
    }
}
