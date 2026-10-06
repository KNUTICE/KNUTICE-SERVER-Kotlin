package com.fx.api.application.service.dashboard

import com.fx.api.application.port.`in`.dashboard.DashboardQueryUseCase
import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.application.port.out.batch.BatchSchedulePersistencePort
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.domain.BatchScheduleDetail
import com.fx.api.domain.Dashboard
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/** 관리자 첫 화면. 화면이 30초마다 다시 부르므로 건수는 DB 에서 한 번에 센다. */
@Service
@Transactional(readOnly = true)
class DashboardQueryService(
    private val batchSchedulePersistencePort: BatchSchedulePersistencePort,
    private val batchExecutionPersistencePort: BatchExecutionPersistencePort,
    private val noticePersistencePort: NoticePersistencePort,
    private val clock: Clock,
) : DashboardQueryUseCase {

    override fun getDashboard(): Dashboard {
        val now = LocalDateTime.now(clock)
        val (enabled, disabled) = batchSchedulePersistencePort.findAll().partition {
            it.enabled
        }
        // 다음 실행 시각은 폴러가 처음 볼 때 채우므로 아직 없을 수 있다 (맨 뒤)
        val upcoming = enabled.sortedWith(
            compareBy(nullsLast()) {
                it.nextFireAt
            }
        )
        val lastExecutions = batchExecutionPersistencePort.findLastExecutions(
            upcoming.map {
                it.scheduleKey
            }
        )
        val counts = batchExecutionPersistencePort.countFailedAndRunning(failedSince = now.minusHours(24))

        return Dashboard(
            failedLast24h = counts.failed,
            running = counts.running,
            pendingSummaries = noticePersistencePort.countPendingSummaries(),
            disabledSchedules = disabled.size,
            upcoming = upcoming.map {
                BatchScheduleDetail(it, lastExecutions[it.scheduleKey])
            },
            recentExecutions = batchExecutionPersistencePort.findExecutions(null, null, PageRequest.of(0, RECENT_EXECUTION_SIZE)).content,
        )
    }

    companion object {
        private const val RECENT_EXECUTION_SIZE = 8
    }

}
