package com.fx.api.adapter.`in`.web.fcmtoken.dto

import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenLanguageUpdateCommand
import com.fx.common.domain.i18n.Language
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import jakarta.validation.constraints.NotBlank

data class FcmTokenLanguageUpdateRequest(

    @field:NotBlank(message = "언어는 필수입니다.")
    val language: String,

) {

    /**
     * 지원 언어(`ko` · `en` · `ja`)로 바꾼다. 대소문자 · 지역 코드는 무시한다 (`JA`, `ja-JP`, `ja_JP` → `ja`).
     * @throws FcmTokenException 지원하지 않는 언어일 때 (LANGUAGE_NOT_SUPPORTED)
     */
    fun toCommand(fcmToken: String) =
        FcmTokenLanguageUpdateCommand(
            fcmToken = fcmToken,
            language = Language.fromOrNull(language) ?: throw FcmTokenException(FcmTokenErrorCode.LANGUAGE_NOT_SUPPORTED),
        )

}
