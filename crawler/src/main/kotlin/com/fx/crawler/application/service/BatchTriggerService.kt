package com.fx.crawler.application.service

import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.SlackType
import com.fx.common.domain.batch.BATCH_REJECT_REASON_MAX_LENGTH
import com.fx.common.domain.batch.BatchJobParameters
import com.fx.common.domain.batch.BatchSchedule
import com.fx.crawler.application.port.`in`.BatchTriggerUseCase
import com.fx.crawler.application.port.out.BatchRunRequestPersistencePort
import com.fx.crawler.application.port.out.BatchSchedulePersistencePort
import com.fx.crawler.application.port.out.JobLaunchPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.batch.JobLaunchOutcome
import com.fx.crawler.domain.batch.JobLaunchRequest
import com.fx.crawler.domain.batch.JobTrigger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

/**
 * 스케줄 · 수동 요청을 Job 실행으로 바꾼다.
 *
 * - 발화와 요청은 조건부 UPDATE 로 선점한 뒤 실행하므로 인스턴스가 여러 개여도 한 번만 실행된다.
 * - 놓친 발화(서버 중단 등)는 한 번만 실행하고, 다음 발화 시각은 지금 이후로 다시 계산한다.
 * - 같은 작업(Job 이름 + 업무 파라미터)이 실행 중이면 이번 발화는 건너뛰고, 수동 요청은 거절한다.
 * - 원격 · 장시간 작업을 기다리지 않도록 트랜잭션을 걸지 않는다. 각 저장은 영속성 어댑터에서 끝난다.
 */
@Service
class BatchTriggerService(
    private val batchSchedulePersistencePort: BatchSchedulePersistencePort,
    private val batchRunRequestPersistencePort: BatchRunRequestPersistencePort,
    private val jobLaunchPort: JobLaunchPort,
    private val webhookPort: WebhookPort,
    private val properties: CrawlerProperties,
    private val clock: Clock,
) : BatchTriggerUseCase {

    private val log = LoggerFactory.getLogger(BatchTriggerService::class.java)

    override fun triggerDueSchedules() {
        val now = LocalDateTime.now(clock)
        batchSchedulePersistencePort.findUninitialized().forEach {
            initialize(it, now)
        }
        batchSchedulePersistencePort.findDue(now).forEach {
            fire(it, now)
        }
    }

    override fun processRunRequests() {
        batchRunRequestPersistencePort.findRequested(properties.poller.requestBatchSize).forEach { request ->
            val now = LocalDateTime.now(clock)
            val requestId = requireNotNull(request.id)
            if (!batchRunRequestPersistencePort.claim(requestId, now)) {
                return@forEach
            }

            val outcome = try {
                launch(
                    JobLaunchRequest(
                        jobName = request.jobName,
                        parameters = BatchJobParameters.parse(request.jobParameters),
                        trigger = JobTrigger.Manual(requestId, request.requestedBy),
                    )
                )
            } catch (e: IllegalArgumentException) {
                JobLaunchOutcome.Rejected(e.message ?: "Job 파라미터가 올바르지 않습니다.")
            }

            when (outcome) {
                is JobLaunchOutcome.Launched -> {
                    batchRunRequestPersistencePort.recordJobExecution(requestId, outcome.jobExecutionId, now)
                    log.info("수동 실행 - request: {}, job: {}, execution: {}", requestId, request.jobName, outcome.jobExecutionId)
                }
                is JobLaunchOutcome.Rejected -> {
                    batchRunRequestPersistencePort.reject(requestId, outcome.reason.take(BATCH_REJECT_REASON_MAX_LENGTH), now)
                    log.warn("수동 실행 거절 - request: {}, job: {}, reason: {}", requestId, request.jobName, outcome.reason)
                }
            }
        }
    }

    override fun recoverInterruptedJobs() {
        val recovered = jobLaunchPort.recoverInterrupted()
        if (recovered.isNotEmpty()) {
            log.warn("실행 중에 멈춘 JobExecution 을 실패로 정리했습니다: {}", recovered)
            notifySlack(SlackType.ERROR, "crawler 재시작으로 실행 중에 멈춘 Job 을 실패로 정리했습니다. JobExecution: $recovered")
        }
    }

    private fun initialize(schedule: BatchSchedule, now: LocalDateTime) {
        val next = nextFireTime(schedule, now) ?: return
        batchSchedulePersistencePort.initializeNextFireAt(requireNotNull(schedule.id), next, now)
    }

    private fun fire(schedule: BatchSchedule, now: LocalDateTime) {
        val scheduledAt = schedule.nextFireAt ?: return
        val next = nextFireTime(schedule, now) ?: return
        if (!batchSchedulePersistencePort.claim(requireNotNull(schedule.id), scheduledAt, next, now)) {
            return
        }

        val outcome = try {
            launch(
                JobLaunchRequest(
                    jobName = schedule.jobName,
                    parameters = BatchJobParameters.parse(schedule.jobParameters),
                    trigger = JobTrigger.Scheduled(schedule.scheduleKey, scheduledAt),
                )
            )
        } catch (e: IllegalArgumentException) {
            JobLaunchOutcome.Rejected(e.message ?: "Job 파라미터가 올바르지 않습니다.")
        }

        when (outcome) {
            is JobLaunchOutcome.Launched ->
                log.info("자동 실행 - schedule: {}, execution: {}", schedule.scheduleKey, outcome.jobExecutionId)
            is JobLaunchOutcome.Rejected -> {
                log.warn("자동 실행 건너뜀 - schedule: {}, reason: {}", schedule.scheduleKey, outcome.reason)
                if (outcome.reason != ALREADY_RUNNING) {
                    notifySlack(SlackType.ERROR, "*Schedule* : ${schedule.scheduleKey}\n*Reason* : ${outcome.reason}")
                }
            }
        }
    }

    private fun launch(request: JobLaunchRequest): JobLaunchOutcome {
        if (!jobLaunchPort.exists(request.jobName)) {
            return JobLaunchOutcome.Rejected("등록되지 않은 Job 입니다: ${request.jobName}")
        }
        if (jobLaunchPort.isRunning(request.jobName, request.parameters)) {
            return JobLaunchOutcome.Rejected(ALREADY_RUNNING)
        }
        return try {
            JobLaunchOutcome.Launched(jobLaunchPort.launch(request))
        } catch (e: Exception) {
            log.error("Job 실행 실패 - job: {}", request.jobName, e)
            JobLaunchOutcome.Rejected("Job 을 시작하지 못했습니다: ${e.message}")
        }
    }

    /** 잘못 저장된 cron 이면 그 스케줄만 건너뛴다 (관리자 API 가 저장 전에 검증한다). */
    private fun nextFireTime(schedule: BatchSchedule, now: LocalDateTime): LocalDateTime? =
        try {
            schedule.batchCron().next(now)
        } catch (e: RuntimeException) {
            log.error("스케줄의 cron 을 해석할 수 없습니다 - schedule: {}, cron: {}", schedule.scheduleKey, schedule.cron, e)
            null
        }

    private fun notifySlack(type: SlackType, content: String) {
        webhookPort.notifySlack(SlackMessage.create(content, type))
    }

    companion object {
        const val ALREADY_RUNNING = "같은 작업이 이미 실행 중입니다."
    }

}
