package com.fx.crawler.domain.push

import com.fx.common.domain.i18n.Language

/** 발송 대상 토큰. [language] 는 저장된 문자열 그대로이며 발송할 때 [resolveLanguage] 로 해석한다. */
data class PushTarget(
    val fcmTokenId: Long,
    val token: String,
    val language: String,
) {

    fun resolveLanguage(): Language = Language.from(language)

}
