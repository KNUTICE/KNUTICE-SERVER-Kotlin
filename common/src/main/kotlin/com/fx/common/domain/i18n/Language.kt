package com.fx.common.domain.i18n

import java.util.Locale

/**
 * 지원 언어.
 *
 * 외부에서 들어오는 언어 값(요청 locale, FCM 토큰에 저장된 문자열)은 [from] 으로 해석한다.
 * 대소문자 · 지역 코드를 무시하고(`JA`, `ja-JP`, `ja_JP` → [JA]), 지원하지 않거나 알 수 없는 값은 [DEFAULT] 로 대체한다.
 * 예외를 던지지 않으므로 잘못된 값이 들어 있어도 조회 · 발송이 깨지지 않는다.
 */
enum class Language(val code: String) {

    KO("ko"),
    EN("en"),
    JA("ja");

    companion object {

        val DEFAULT: Language = KO

        fun from(code: String?): Language =
            fromOrNull(code) ?: DEFAULT

        /**
         * [from] 과 같이 해석하되, 지원하지 않거나 알 수 없는 값이면 null 이다.
         * 사용자가 고른 언어를 저장할 때처럼 한국어로 대체하면 안 되는 곳에서 쓴다.
         */
        fun fromOrNull(code: String?): Language? {
            val languageCode = code
                ?.trim()
                ?.substringBefore('-')
                ?.substringBefore('_')
                ?.lowercase(Locale.ROOT)
            return entries.firstOrNull {
                it.code == languageCode
            }
        }

        fun from(locale: Locale?): Language =
            from(locale?.language)

    }

}
