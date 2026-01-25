package com.edu.platform.course.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/Swagger 配置类
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("课程服务API文档")
                        .description("Course Service API Documentation")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Admin")
                                .email("admin@example.com")
                                .url("https://www.example.com")));
    }
}
