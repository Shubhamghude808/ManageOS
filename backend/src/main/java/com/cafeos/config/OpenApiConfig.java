package com.cafeos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI cafeOsApi() {
        return new OpenAPI().info(new Info().title("CafeOS API").version("v1")
                .description("Local-first gaming cafe management API"));
    }
}
