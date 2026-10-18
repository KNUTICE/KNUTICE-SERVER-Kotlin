package com.fx.api.config.security

import com.fx.api.adapter.out.security.JwtProvider
import com.fx.api.application.port.out.user.JwtProviderPort
import com.fx.api.domain.UserRole
import com.fx.api.exception.handler.AuthExceptionHandler
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.DispatcherType
import jakarta.servlet.Filter
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Date

/**
 * 보안 필터 체인의 인증 · 인가 응답을 검증한다.
 * 앱 전체(JPA 등) 없이 [SecurityConfig] · 예외 핸들러 · 테스트 컨트롤러만 올린다.
 */
@SpringJUnitWebConfig(SecurityConfigTest.TestConfig::class)
@TestPropertySource(properties = ["url.allowed-origins[0]=http://localhost:5173", "url.allowed-origins[1]=https://knutice.com"])
class SecurityConfigTest @Autowired constructor(
    context: WebApplicationContext,
) {

    private val mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters<DefaultMockMvcBuilder>(context.getBean("springSecurityFilterChain", Filter::class.java))
        .build()

    @Test
    fun `관리자 토큰이면 관리자 API 를 통과한다`() {
        mockMvc.get(ADMIN_API) {
            header(HttpHeaders.AUTHORIZATION, bearer(providerAt(NOW).generateAccessToken("1", UserRole.ADMIN)))
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `토큰이 없으면 401 이다`() {
        mockMvc.get(ADMIN_API)
            .andExpectAuthError(401, "로그인이 필요합니다.")
    }

    @Test
    fun `만료된 토큰이면 만료 사유로 401 이다`() {
        val expired = providerAt(NOW.minus(Duration.ofHours(2))).generateAccessToken("1", UserRole.ADMIN)

        mockMvc.get(ADMIN_API) {
            header(HttpHeaders.AUTHORIZATION, bearer(expired))
        }.andExpectAuthError(401, "만료된 토큰입니다.")
    }

    @Test
    fun `토큰 종류가 없는 예전 토큰은 서명이 맞아도 401 이다`() {
        val legacy = Jwts.builder()
            .claim("userId", "1")
            .claim("role", "ADMIN")
            .expiration(Date.from(NOW.plus(Duration.ofDays(7))))
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.toByteArray()))
            .compact()

        mockMvc.get(ADMIN_API) {
            header(HttpHeaders.AUTHORIZATION, bearer(legacy))
        }.andExpectAuthError(401, "유효하지 않은 토큰입니다.")
    }

    @Test
    fun `관리자가 아니면 403 이다`() {
        mockMvc.get(ADMIN_API) {
            header(HttpHeaders.AUTHORIZATION, bearer(providerAt(NOW).generateAccessToken("1", UserRole.USER)))
        }.andExpectAuthError(403, "권한이 없습니다.")
    }

    @Test
    fun `Open API 는 토큰 없이 통과한다`() {
        mockMvc.get(OPEN_API)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `처리되지 않은 예외의 오류 재디스패치는 인증 오류로 바뀌지 않는다`() {
        mockMvc.get("/error") {
            with {
                it.apply {
                    dispatcherType = DispatcherType.ERROR
                }
            }
        }.andExpect {
            status { isInternalServerError() }
        }
    }

    private fun ResultActionsDsl.andExpectAuthError(httpStatus: Int, message: String) =
        andExpect {
            status { isEqualTo(httpStatus) }
            jsonPath("$.metaData.success") { value(false) }
            jsonPath("$.metaData.code") { value(httpStatus) }
            jsonPath("$.metaData.message") { value(message) }
        }

    private fun bearer(token: String) =
        "Bearer $token"

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import(SecurityConfig::class, AuthExceptionHandler::class, TestController::class)
    class TestConfig {

        @Bean
        fun jwtProviderPort(): JwtProviderPort =
            providerAt(NOW)

    }

    @RestController
    class TestController {

        @GetMapping(ADMIN_API)
        fun admin(): String =
            "admin"

        @GetMapping(OPEN_API)
        fun open(): String =
            "open"

        /** 서블릿 컨테이너가 예외를 넘기는 오류 화면 대신 */
        @GetMapping("/error")
        fun error(): ResponseEntity<String> =
            ResponseEntity.internalServerError().body("error")

    }

    companion object {

        private const val ADMIN_API = "/api/v1/test"
        private const val OPEN_API = "/open-api/v1/test"
        private const val SECRET_KEY = "test-secret-key-test-secret-key-test-secret-key"

        private val NOW = Instant.parse("2026-10-15T00:00:00Z")

        private fun providerAt(instant: Instant): JwtProvider =
            JwtProvider(
                AuthProperties(
                    jwtSecretKey = SECRET_KEY,
                    accessTokenTtl = Duration.ofHours(1),
                    refreshTokenTtl = Duration.ofDays(14),
                ),
                Clock.fixed(instant, ZoneId.of("Asia/Seoul")),
            )

    }

}
