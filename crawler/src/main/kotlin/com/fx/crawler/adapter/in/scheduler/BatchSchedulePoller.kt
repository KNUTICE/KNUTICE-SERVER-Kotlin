package com.fx.crawler.adapter.`in`.scheduler

import com.fx.crawler.application.port.`in`.BatchTriggerUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled

/**
 * 배치 실행의 유일한 `@Scheduled`. 1분마다 DB 의 스케줄 · 수동 실행 요청을 확인해 Job 을 실행한다.
 * 실행 시각은 코드가 아니라 `batch_schedule.cron` 으로 정한다.
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

    @Scheduled(fixedDelayString = "60s", initialDelayString = "10s")
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
