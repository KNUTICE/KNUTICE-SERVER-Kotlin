package com.fx.crawler.adapter.`in`.scheduler

import com.fx.crawler.application.port.`in`.BatchTriggerUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled

/**
 * 배치 실행의 유일한 `@Scheduled`. 매분 DB 의 스케줄 · 수동 실행 요청을 확인해 Job 을 실행한다.
 * 실행 시각은 코드가 아니라 `batch_schedule.cron` 으로 정한다.
 *
 * 스케줄 cron 은 모두 분 단위(초 = 0)이므로 매분 정각 직후에 확인하면 Job 이 cron 시각에 맞춰 시작한다.
 * 정각에 딱 맞추지 않고 1초 뒤로 두는 것은, 시계 오차로 정각 직전에 깨어나 그 분의 발화를 다음 분으로 미루지 않기 위해서다.
 * `fixedDelay` 는 기동 시각에 따라 확인 시각(초)이 정해지고 처리 시간만큼 계속 밀리므로 쓰지 않는다.
 *
 * 기동할 때(스케줄 작업이 시작되기 전) 이전 프로세스가 비정상 종료돼 실행 중으로 남은 Job 을 먼저 정리한다.
 * 정리하지 않으면 같은 작업이 계속 실행 중으로 보여 스케줄이 영영 건너뛰어진다.
 * crawler 는 인스턴스 하나로 운영한다는 전제다 (다른 인스턴스가 실행 중인 Job 도 정리해 버린다).
 */
@ScheduleAdapter
@ConditionalOnProperty(prefix = "crawler.poller", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class BatchSchedulePoller(
    private val batchTriggerUseCase: BatchTriggerUseCase,
) : SmartInitializingSingleton {

    private val log = LoggerFactory.getLogger(BatchSchedulePoller::class.java)

    override fun afterSingletonsInstantiated() {
        batchTriggerUseCase.recoverInterruptedJobs()
    }

    @Scheduled(cron = "1 * * * * *")
    fun poll() {
        runCatching {
            batchTriggerUseCase.triggerDueSchedules()
        }.onFailure {
            log.error("스케줄 실행 중 오류", it)
        }
        runCatching {
            batchTriggerUseCase.processRunRequests()
        }.onFailure {
            log.error("수동 실행 요청 처리 중 오류", it)
        }
    }

}
