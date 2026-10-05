package com.fx.api.adapter.`in`.web.fcmtoken.dto

import com.fx.common.domain.i18n.Language

/** @property language 알림 언어 코드 (`ko` · `en` · `ja`) */
data class FcmTokenLanguageResponse(
    val language: String,
) {

    companion object {
        fun from(language: Language) =
            FcmTokenLanguageResponse(language.code)
    }

}
