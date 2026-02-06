package com.fx.api.config.web

import com.fx.api.config.interceptor.ApiLogInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
class WebConfig(
    @Value("\${file.upload-dir}") private val uploadDir: Path,
    @Value("\${file.context-path}") private val contextPath: String,
    private val apiLogInterceptor: ApiLogInterceptor
) : WebMvcConfigurer {

    private val EXCLUDE_PATHS = arrayOf(
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/**",
        "/$contextPath**"
    )

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("/$contextPath**")
            .addResourceLocations("file:$uploadDir")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiLogInterceptor)
            .addPathPatterns("/api/**", "/open-api/**")
            .excludePathPatterns(*EXCLUDE_PATHS)
    }

}