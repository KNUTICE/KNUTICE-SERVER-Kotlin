package com.fx.api.config.swagger

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

}