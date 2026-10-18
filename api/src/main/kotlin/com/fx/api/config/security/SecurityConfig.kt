package com.fx.api.config.security

import com.fx.api.application.port.out.user.JwtProviderPort
import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.HandlerExceptionResolver

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProviderPort: JwtProviderPort,
    @param:Qualifier("handlerExceptionResolver") private val handlerExceptionResolver: HandlerExceptionResolver,
    @param:Value("\${url.allowed-origins[0]}") private val allowedOrigin0: String,
    @param:Value("\${url.allowed-origins[1]}") private val allowedOrigin1: String,
) {

    private val WHITE_LIST = arrayOf(
        "/open-api/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/**"
    )

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val authErrorResponder = AuthErrorResponder(handlerExceptionResolver)
        http
            // 액세스 토큰은 헤더로 받고, 쿠키로 받는 리프레시 토큰은 SameSite=Strict 라 다른 사이트의 요청에 실리지 않는다
            .csrf {
                it.disable()
            }
            .cors {  }
            .formLogin {
                it.disable()
            }
            .httpBasic {
                it.disable()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authErrorResponder)
                it.accessDeniedHandler(authErrorResponder)
            }
            .authorizeHttpRequests { auth ->
                auth
                    // 원래 요청에서 이미 인가를 거친 재디스패치다. ERROR(/error)를 막으면 처리되지 않은 예외가 500 대신 인증 오류로 나간다
                    .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                    .requestMatchers(*WHITE_LIST).permitAll()
                    .requestMatchers("/api/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtProviderPort),
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        val allowedOrigins = listOf(allowedOrigin0, allowedOrigin1)
        config.allowedOriginPatterns = allowedOrigins
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }

}