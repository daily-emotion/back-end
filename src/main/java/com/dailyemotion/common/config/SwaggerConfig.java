package com.dailyemotion.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "Authorization";

        // JWT 보안 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .components(components)
                .security(List.of(securityRequirement))
                .info(apiInfo())
                .servers(List.of(  // 서버 URL 순서 지정
                        new Server().url("https://dailymotion.site/api").description("Production Server"),
                        new Server().url("http://localhost:8080/api").description("Local Server")
                ))
                .tags(List.of(  // 태그 순서 지정
                        new Tag().name("1. User Controller").description("User API"),
                        new Tag().name("2. Diary Controller").description("Diary API"),
                        new Tag().name("3. Statistics Controller").description("Statistics API")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("DailyEmotion Swagger")
                .description("DailyEmotion REST API")
                .version("1.0.0");
    }
}
