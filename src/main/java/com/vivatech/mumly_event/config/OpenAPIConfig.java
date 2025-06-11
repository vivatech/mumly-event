package com.vivatech.mumly_event.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BasicAuth"))
                .components(new Components().addSecuritySchemes("BasicAuth",
                        new SecurityScheme()
                                .name("BasicAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                ))
                .info(new Info().title("Mumly Event Management System")
                .version("v1")
                .description("API Documentation for event management system"))
                .externalDocs(new ExternalDocumentation()
                        .description("Learn more"));
    }
}
