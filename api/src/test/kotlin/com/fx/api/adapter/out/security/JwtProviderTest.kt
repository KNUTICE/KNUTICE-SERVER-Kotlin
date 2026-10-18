package com.fx.api.adapter.out.security

import com.fx.api.config.security.AuthProperties
import com.fx.api.domain.UserRole
import com.fx.api.exception.AuthException
import com.fx.api.exception.errorcode.AuthErrorCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Date

class JwtProviderTest : BehaviorSpec({

    val secretKey = "test-secret-key-test-secret-key-test-secret-key"
    val properties = AuthProperties(
        jwtSecretKey = secretKey,
        accessTokenTtl = Duration.ofHours(1),
        refreshTokenTtl = Duration.ofDays(14),
    )
    val issuedAt = Instant.parse("2026-10-15T00:00:00Z")
    val zone = ZoneId.of("Asia/Seoul")

    fun providerAt(instant: Instant) =
        JwtProvider(properties, Clock.fixed(instant, zone))

    val token = providerAt(issuedAt).generateAccessToken("123", UserRole.ADMIN)

    fun shouldFailWith(errorCode: AuthErrorCode, block: () -> Unit) {
        shouldThrow<AuthException> {
            block()
        }.baseErrorCode shouldBe errorCode
    }

    Given("발급한 액세스 토큰") {
        Then("유효 시간 안에는 사용자 정보를 꺼낸다") {
            val user = providerAt(issuedAt.plus(Duration.ofMinutes(59))).authenticate(token)
            user.userId shouldBe "123"
            user.role shouldBe UserRole.ADMIN
        }

        Then("유효 시간이 지나면 만료 오류다") {
            shouldFailWith(AuthErrorCode.EXPIRED_TOKEN) {
                providerAt(issuedAt.plus(Duration.ofHours(1)).plusSeconds(1)).authenticate(token)
            }
        }

        Then("다른 키로 서명했으면 잘못된 토큰이다") {
            val otherKeyToken = JwtProvider(
                properties.copy(jwtSecretKey = "other-secret-key-other-secret-key-other-secret"),
                Clock.fixed(issuedAt, zone),
            ).generateAccessToken("123", UserRole.ADMIN)

            shouldFailWith(AuthErrorCode.INVALID_TOKEN) {
                providerAt(issuedAt).authenticate(otherKeyToken)
            }
        }
    }

    Given("토큰 종류 클레임이 없는 토큰") {
        // 리프레시 토큰도 같은 키 · 클레임의 JWT 로 발급하던 때의 토큰
        val legacyToken = Jwts.builder()
            .claim("userId", "123")
            .claim("role", "ADMIN")
            .expiration(Date.from(issuedAt.plus(Duration.ofDays(7))))
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()

        Then("서명이 맞아도 받지 않는다") {
            shouldFailWith(AuthErrorCode.INVALID_TOKEN) {
                providerAt(issuedAt).authenticate(legacyToken)
            }
        }
    }

    Given("JWT 가 아닌 값") {
        Then("잘못된 토큰이다") {
            shouldFailWith(AuthErrorCode.INVALID_TOKEN) {
                providerAt(issuedAt).authenticate("not-a-jwt")
            }
        }
    }

})
