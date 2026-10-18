package com.fx.api.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import java.time.Duration
import java.time.LocalDateTime

class RefreshTokenTest : BehaviorSpec({

    val now = LocalDateTime.of(2026, 10, 15, 9, 0)
    val lifetime = Duration.ofDays(14)

    Given("토큰 원문") {
        Then("매번 다른 URL-safe 문자열이다 (32바이트)") {
            val token = RefreshToken.generateToken()
            token shouldMatch Regex("[A-Za-z0-9_-]{43}")
            RefreshToken.generateToken() shouldNotBe token
        }

        Then("해시는 같은 원문이면 같고, 컬럼 길이(64)의 hex 다") {
            RefreshToken.hash("token") shouldBe RefreshToken.hash("token")
            RefreshToken.hash("token") shouldHaveLength REFRESH_TOKEN_HASH_LENGTH
            RefreshToken.hash("token") shouldMatch Regex("[0-9a-f]{64}")
        }
    }

    Given("발급") {
        val refreshToken = RefreshToken.issue(1L, "token", now, lifetime)

        Then("원문이 아니라 해시를 저장하고, 유효 시간 뒤에 만료된다") {
            refreshToken.userId shouldBe 1L
            refreshToken.tokenHash shouldBe RefreshToken.hash("token")
            refreshToken.previousTokenHash shouldBe null
            refreshToken.isExpired(now.plus(lifetime).minusNanos(1)) shouldBe false
            refreshToken.isExpired(now.plus(lifetime)) shouldBe true
        }
    }

    Given("재발급(회전)") {
        val refreshToken = RefreshToken.issue(1L, "old", now, lifetime)
        val later = now.plusDays(10)

        refreshToken.rotate("new", later, lifetime)

        Then("새 토큰으로 바꾸고 직전 토큰을 남긴다") {
            refreshToken.tokenHash shouldBe RefreshToken.hash("new")
            refreshToken.previousTokenHash shouldBe RefreshToken.hash("old")
        }

        Then("만료를 재발급 시각부터 다시 센다") {
            refreshToken.expiresAt shouldBe later.plus(lifetime)
            refreshToken.isExpired(now.plus(lifetime)) shouldBe false
        }
    }

})
