package com.fx.api.domain

/** 관리자 첫 화면에 보여 줄 배치 · 요약 현황 */
class Dashboard(
    /** 24시간 안에 실패로 끝난 Job 실행 수 */
    val failedLast24h: Long,
    /** 지금 실행 중인 Job 실행 수 */
    val running: Long,
    /** AI 요약을 기다리는 공지 수 */
    val pendingSummaries: Long,
    /** 자동 실행이 꺼진 스케줄 수 */
    val disabledSchedules: Int,
    /** 켜진 스케줄. 다음 실행이 빠른 순 */
    val upcoming: List<BatchScheduleDetail>,
    /** 최근 Job 실행. 최신순 */
    val recentExecutions: List<BatchJobExecution>,
)
