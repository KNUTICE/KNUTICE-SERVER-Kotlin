package com.fx.readingroom.domain

import com.fx.common.domain.i18n.Language

/** 빈자리 확인 · 발송에 쓰는 알림 읽기 모델. 알림을 건 토큰과 알림 언어를 함께 담는다. */
data class SeatAlertTarget(
    val seatAlertId: Long,
    val fcmTokenId: Long,
    val token: String,
    val language: String,
    val readingRoom: ReadingRoom,
    val seatNumber: Int,
) {

    fun resolveLanguage(): Language =
        Language.from(language)

}
