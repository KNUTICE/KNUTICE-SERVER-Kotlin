package com.fx.api.application.service.question

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import java.time.Instant

class NoticeQuestionSignerTest : BehaviorSpec({

    val signer = NoticeQuestionSigner("test-secret-at-least-32-characters-long")
    val expiresAt = Instant.parse("2026-11-20T01:10:00Z")

    Given("기기 키") {
        Then("같은 토큰은 같은 64자 해시, 원문은 들어 있지 않다") {
            val key = signer.deviceKey("fcm-token-abc")
            key.length shouldBe 64
            key shouldBe signer.deviceKey("fcm-token-abc")
            key shouldNotBe signer.deviceKey("fcm-token-abd")
            key shouldNotContain "fcm-token"
        }

        Then("비밀값이 다르면 해시도 다르다") {
            NoticeQuestionSigner("another-secret-at-least-32-characters").deviceKey("fcm-token-abc") shouldNotBe signer.deviceKey("fcm-token-abc")
        }
    }

    Given("답변 저장 서명") {
        val token = signer.finishToken(77, "device", expiresAt)

        Then("같은 질문 · 같은 사람 · 만료 전이면 통과한다") {
            signer.verifyFinishToken(token, 77, "device", expiresAt.minusSeconds(1)) shouldBe true
            signer.verifyFinishToken(token, 77, "device", expiresAt) shouldBe true
        }

        Then("만료 뒤 · 다른 질문 · 다른 사람 · 고친 서명이면 거절한다") {
            signer.verifyFinishToken(token, 77, "device", expiresAt.plusSeconds(1)) shouldBe false
            signer.verifyFinishToken(token, 78, "device", expiresAt) shouldBe false
            signer.verifyFinishToken(token, 77, "other", expiresAt) shouldBe false
            signer.verifyFinishToken(token.dropLast(1) + "x", 77, "device", expiresAt) shouldBe false
            // 만료 시각만 늘린 서명
            val extended = "${expiresAt.epochSecond + 3600}.${token.substringAfter('.')}"
            signer.verifyFinishToken(extended, 77, "device", expiresAt) shouldBe false
            signer.verifyFinishToken("garbage", 77, "device", expiresAt) shouldBe false
        }
    }

    Given("비밀값이 짧으면") {
        Then("뜨지 않는다") {
            shouldThrow<IllegalArgumentException> {
                NoticeQuestionSigner("short")
            }
        }
    }

})
