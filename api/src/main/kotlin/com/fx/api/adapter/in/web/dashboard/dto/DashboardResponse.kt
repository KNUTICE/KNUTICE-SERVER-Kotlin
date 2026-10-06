package com.fx.api.adapter.`in`.web.dashboard.dto

import com.fx.api.adapter.`in`.web.batch.dto.BatchExecutionResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleResponse
import com.fx.api.domain.Dashboard

data class DashboardResponse(
    /** 24시간 안에 실패로 끝난 Job 실행 수 */
    val failedLast24h: Long,
    /** 지금 실행 중인 Job 실행 수 */
    val running: Long,
    /** AI 요약을 기다리는 공지 수 */
    val pendingSummaries: Long,
    /** 자동 실행이 꺼진 스케줄 수 */
    val disabledSchedules: Int,
    /** 켜진 스케줄. 다음 실행이 빠른 순 */
    val upcoming: List<BatchScheduleResponse>,
    /** 최근 Job 실행 8건. 최신순 */
    val recentExecutions: List<BatchExecutionResponse>,
) {

    companion object {

        fun from(dashboard: Dashboard): DashboardResponse =
            DashboardResponse(
                failedLast24h = dashboard.failedLast24h,
                running = dashboard.running,
                pendingSummaries = dashboard.pendingSummaries,
                disabledSchedules = dashboard.disabledSchedules,
                upcoming = BatchScheduleResponse.from(dashboard.upcoming),
                recentExecutions = dashboard.recentExecutions.map {
                    BatchExecutionResponse.from(it)
                },
            )

    }

}
