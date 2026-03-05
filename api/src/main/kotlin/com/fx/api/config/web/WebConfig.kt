package com.fx.api.config.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.nio.file.Path

@Configuration
class WebConfig(
    @Value("\${file.upload-dir}") private val uploadDir: Path,
    @Value("\${file.context-path}") private val contextPath: String
) : WebFluxConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("/$contextPath**")
            .addResourceLocations("file:$uploadDir")
    }

}