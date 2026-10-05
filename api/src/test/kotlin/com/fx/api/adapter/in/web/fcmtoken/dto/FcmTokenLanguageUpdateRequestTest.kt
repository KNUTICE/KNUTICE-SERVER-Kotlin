package com.fx.api.adapter.`in`.web.fcmtoken.dto

import com.fx.common.domain.i18n.Language
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class FcmTokenLanguageUpdateRequestTest : BehaviorSpec({

    Given("지원 언어로 보내면") {
        When("대소문자 · 지역 코드가 섞여 있어도") {
            Then("지원 언어로 바꾼다") {
                mapOf(
                    "ko" to Language.KO, "en" to Language.EN, "ja" to Language.JA,
                    "JA" to Language.JA, "ja-JP" to Language.JA, "ja_JP" to Language.JA, "en-US" to Language.EN,
                ).forEach { (value, expected) ->
                    FcmTokenLanguageUpdateRequest(value).toCommand("fcm-token").language shouldBe expected
                }
            }
        }
    }

    Given("지원하지 않는 언어로 보내면") {
        When("변환하면") {
            Then("LANGUAGE_NOT_SUPPORTED 예외가 발생한다") {
                listOf("zh", "fr-FR", "japanese").forEach {
                    shouldThrow<FcmTokenException> {
                        FcmTokenLanguageUpdateRequest(it).toCommand("fcm-token")
                    }.baseErrorCode shouldBe FcmTokenErrorCode.LANGUAGE_NOT_SUPPORTED
                }
            }
        }
    }

    Given("빈 값으로 보내면") {
        When("검증하면") {
            Then("거절한다") {
                val validator = Validation.buildDefaultValidatorFactory().validator
                listOf("", " ").forEach {
                    validator.validate(FcmTokenLanguageUpdateRequest(it)).shouldNotBeEmpty()
                }
            }
        }
    }

})
