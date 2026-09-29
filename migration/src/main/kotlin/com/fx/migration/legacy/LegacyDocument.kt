package com.fx.migration.legacy

import org.bson.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * 레거시 도큐먼트를 필드 이름으로 읽는다.
 *
 * 레거시 앱(Spring Data MongoDB)은 `LocalDateTime` · `LocalDate` 를 시스템 타임존([zone]) 기준 시각으로 보고
 * UTC `Date` 로 저장했다. 그래서 `LocalDate` 2026-09-29 는 `2026-09-28T15:00:00Z` 로 들어 있다.
 * 같은 타임존으로 되돌려야 원래 값이 나오므로, 러너 JVM 의 타임존에 기대지 않고 [zone] 을 명시해 바꾼다.
 */
class LegacyDocument(
    private val document: Document,
    private val zone: ZoneId,
) {

    val id: Any?
        get() = document["_id"]

    fun string(key: String): String? = document[key]?.toString()

    fun long(key: String): Long? = (document[key] as? Number)?.toLong()

    /** 여러 이름 중 먼저 있는 값. Kotlin `isXxx` 프로퍼티는 저장 이름이 버전마다 다를 수 있어 둘 다 확인한다. */
    fun boolean(vararg keys: String): Boolean? = keys.firstNotNullOfOrNull { document[it] as? Boolean }

    fun dateTime(key: String): LocalDateTime? =
        (document[key] as? Date)?.let { LocalDateTime.ofInstant(it.toInstant(), zone) }

    fun date(key: String): LocalDate? =
        when (val value = document[key]) {
            is Date -> LocalDateTime.ofInstant(value.toInstant(), zone).toLocalDate()
            is String -> runCatching { LocalDate.parse(value) }.getOrNull()
            else -> null
        }

    /** 문자열 배열 (enum 이름 집합 등). 없으면 빈 목록. */
    fun strings(key: String): List<String> =
        (document[key] as? Collection<*>).orEmpty().mapNotNull { it?.toString() }

}
