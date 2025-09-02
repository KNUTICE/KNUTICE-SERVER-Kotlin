package com.fx.api.config.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod

@Configuration
class SwaggerConfig {

    @Bean
    fun customOperationCustomizer(): OperationCustomizer {
        return OperationCustomizer { operation, handlerMethod: HandlerMethod ->
            ApiExceptionExplainParser.parse(operation, handlerMethod)
            operation
        }
    }

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")

        val securityRequirement = SecurityRequirement().addList("Bearer")

        return OpenAPI()
            .components(Components().addSecuritySchemes("Bearer", securityScheme))
            .security(listOf(securityRequirement))
            .info(
                Info()
                    .title("API SERVICE")
                    .version("v1")
            )
    }
}