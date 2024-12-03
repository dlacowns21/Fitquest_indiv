package com.web.fitquest.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// http://localhost:8097/fitquest/swagger-ui/index.html#/

@Configuration
public class SwaggerConfiguration {
    
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("FitQuest API 명세서")
                .description("FitQuest의 RESTful API 명세서입니다.")
                .version("v1")
                .contact(new io.swagger.v3.oas.models.info.Contact()
                        .name("FitQuest")
                        .email("fitquest@fitquest.com")
                        .url("http://fitquest.com"));
                        
        return new OpenAPI()
                .components(new Components())
                .info(info);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("fitquest-public")
                .pathsToMatch("/api/**")
                .build();
    }
}