package com.fx.common.domain.statistics

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 통계 날짜의 기준 시간대 (KST).
 * API 로그의 요청 시각과 일일 통계의 날짜를 JVM 기본 시간대와 상관없이 같은 시간대로 정한다.
 */
object StatisticsTime {

    val ZONE: ZoneId =
        ZoneId.of("Asia/Seoul")

    fun now(clock: Clock): LocalDateTime =
        LocalDateTime.now(clock.withZone(ZONE))

    fun today(clock: Clock): LocalDate =
        LocalDate.now(clock.withZone(ZONE))

}
