package com.fx.api.config.security

import com.fx.api.application.port.out.JwtProviderPort
import com.fx.api.config.filter.JwtAuthenticationWebFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val jwtProviderPort: JwtProviderPort,
    @Value("\${url.allowed-origins[0]}") private val allowedOrigin0: String,
    @Value("\${url.allowed-origins[1]}") private val allowedOrigin1: String,
) {

    private val WHITE_LIST = arrayOf(
        "/open-api/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/**"
    )

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { } // CorsWebFilter 사용
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(*WHITE_LIST).permitAll()
                    .pathMatchers("/api/**").hasRole("ADMIN")
                    .anyExchange().authenticated()
            }
            .addFilterAt(
                JwtAuthenticationWebFilter(jwtProviderPort),
                SecurityWebFiltersOrder.AUTHENTICATION
            )
            .build()
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(allowedOrigin0, allowedOrigin1)
            allowedMethods = listOf(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
            )
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return CorsWebFilter(source)
    }

}