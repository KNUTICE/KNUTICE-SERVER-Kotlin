package com.fx.api.config.ktor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fx.global.exception.ConnectionException
import com.fx.global.exception.errorcode.ConnectionErrorCode
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.jackson.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KtorConfig {

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient(CIO) {
            install(HttpCookies) // 쿠키 관리
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }

            // dslee (2026.02.10) : 예외처리 추가
            expectSuccess = true // 2xx 응답만 성공으로 간주, 나머지는 예외 발생
            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    throw ConnectionException(ConnectionErrorCode.REMOTE_SERVER_UNAVAILABLE)
                }
            }
        }
    }

}