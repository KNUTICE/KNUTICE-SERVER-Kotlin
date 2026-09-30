package com.fx.common.domain.notification

/**
 * 알림 문구의 `{이름}` 형식 placeholder 를 다룬다.
 * 이름은 영문자로 시작하고 영문자 · 숫자만 쓴다 (예: `{title}`, `{seatNumber}`).
 */
object TemplatePlaceholder {

    private val PATTERN = Regex("""\{([A-Za-z][A-Za-z0-9]*)}""")

    fun extract(text: String): Set<String> =
        PATTERN.findAll(text).map {
            it.groupValues[1]
        }.toSet()

    /** placeholder 를 [values] 로 채운다. 값이 없는 placeholder 는 그대로 둔다. */
    fun fill(text: String, values: Map<String, Any>): String =
        PATTERN.replace(text) { match ->
            values[match.groupValues[1]]?.toString() ?: match.value
        }

}
