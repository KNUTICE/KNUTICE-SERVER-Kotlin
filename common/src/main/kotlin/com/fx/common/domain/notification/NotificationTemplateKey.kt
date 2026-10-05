package com.fx.common.domain.notification

import com.fx.common.domain.i18n.LocalizedText

/**
 * 코드가 참조하는 알림 문구 키와 키별 placeholder 목록.
 *
 * 문구는 DB(`notification_template`)에서 관리하지만 키 자체는 코드가 쓰므로 여기서만 늘리고 줄인다.
 * 키를 추가하면 Flyway 시드도 함께 추가한다.
 */
enum class NotificationTemplateKey(
    val placeholders: Set<String>,
) {

    /** `{title} 외 {count}개의 소식이 있습니다.` */
    NOTICE_BODY_MULTIPLE(setOf("title", "count")),

    /** `{date} {mealName} 메뉴` */
    MEAL_HEADER(setOf("date", "mealName")),

    /** `[한식]` */
    MEAL_SECTION_KOREAN(emptySet()),

    /** `[일품]` */
    MEAL_SECTION_TOP(emptySet()),

    /** `등록된 식단 정보가 없습니다.` */
    MEAL_EMPTY(emptySet()),

    /** `빈자리 알림` */
    SEAT_ALERT_TITLE(emptySet()),

    /** `{roomName} {seatNumber}번 좌석이 비었습니다!` */
    SEAT_ALERT_BODY(setOf("roomName", "seatNumber"));

    /**
     * 언어별 문구가 이 키의 placeholder 를 정확히 쓰는지 검증한다.
     * 빠진 placeholder 가 있거나 모르는 placeholder 가 있으면 예외를 던진다.
     */
    fun validate(text: LocalizedText) {
        text.values().forEach { (language, value) ->
            val used = TemplatePlaceholder.extract(value)
            val missing = placeholders - used
            val unknown = used - placeholders
            require(missing.isEmpty() && unknown.isEmpty()) {
                "$name(${language.code}) 문구의 placeholder 가 올바르지 않습니다. 빠짐: $missing, 모름: $unknown"
            }
        }
    }

}
