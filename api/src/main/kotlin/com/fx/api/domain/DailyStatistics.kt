package com.fx.api.domain

import java.time.LocalDate
import java.time.LocalDateTime

/** crawler 의 일일 통계 Job 이 집계한 하루치 서비스 요약 (`daily_statistics`). */
data class DailyStatistics(
    val statisticsDate: LocalDate,
    /** 앱 API 요청 수 */
    val requestCount: Long,
    /** 요청한 기기 수. 기기 종류를 모르는 토큰도 세므로 iOS + Android 보다 클 수 있다 */
    val deviceCount: Int,
    val iosDeviceCount: Int,
    val aosDeviceCount: Int,
    /** 그날 저장된 공지 수 */
    val noticeCount: Int,
    /** 그날 공지 중 집계 시점에 요약이 완료된 수 */
    val summaryCompletedCount: Int,
    /** 그날 공지 중 집계 시점에 요약이 실패한 수 */
    val summaryFailedCount: Int,
    /** 그날 등록된 FCM 토큰 수 */
    val newTokenCount: Int,
    /** 이 날짜를 마지막으로 집계한 시각 */
    val aggregatedAt: LocalDateTime,
)

/**
 * 하루가 끝난 직후의 토큰 현황 (`daily_statistics` 의 스냅샷 컬럼).
 *
 * 토큰은 현재 상태만 남으므로 지난 날짜를 다시 계산할 수 없다. 일일 통계 Job 이 실행될 때의 현황을 어제 행에만 기록하므로,
 * Job 이 실행되지 않은 날은 [active] · [inactive] 가 null 이다.
 */
data class DailyTokenStatistics(
    val statisticsDate: LocalDate,
    /** 그날 등록된 FCM 토큰 수 */
    val newTokenCount: Int,
    /** 활성(발송 대상) 토큰. iOS · Android 만 센다 */
    val active: DeviceCounts?,
    /** 비활성 토큰. iOS · Android 만 센다 */
    val inactive: DeviceCounts?,
)

/** 기기 종류별 수. [total] 에는 종류를 모르는 기기도 들어갈 수 있어 [ios] + [aos] 보다 클 수 있다. */
data class DeviceCounts(
    val total: Long,
    val ios: Long,
    val aos: Long,
) {

    companion object {

        val ZERO =
            DeviceCounts(0, 0, 0)

        /** 종류를 모르는 기기를 세지 않은 값 */
        fun of(ios: Long, aos: Long): DeviceCounts =
            DeviceCounts(ios + aos, ios, aos)

    }

}
