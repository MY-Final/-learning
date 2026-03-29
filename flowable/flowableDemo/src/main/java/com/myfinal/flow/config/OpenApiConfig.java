package com.myfinal.flow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 文档基础信息。
     */
    @Bean
    public OpenAPI flowableOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Flowable Demo API")
                .description("流程模型上传与自动部署接口文档")
                .version("v1.0.0"));
    }

    /**
     * Swagger 接口分组：流程模型相关接口。
     */
    @Bean
    public GroupedOpenApi processModelApi() {
        return GroupedOpenApi.builder()
                .group("流程模型接口")
                .pathsToMatch("/api/process-models/**")
                .build();
    }
}
