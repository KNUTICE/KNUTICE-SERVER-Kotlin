package com.fx.crawler.adapter.out.batch

import com.fx.common.domain.batch.BatchJobParameters
import com.fx.crawler.application.port.out.JobLaunchPort
import com.fx.crawler.domain.batch.JobLaunchRequest
import com.fx.crawler.domain.batch.JobTrigger
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.stereotype.Component

/**
 * [JobOperator] 로 Job 을 실행한다. 실행기는 `@BatchTaskExecutor` 로 지정한 비동기 실행기다.
 *
 * - 업무 파라미터(예: `topicType`)는 identifying 이다.
 * - 자동 실행은 발화 시각(`scheduledAt`), 수동 실행은 요청 ID(`requestId`)를 identifying 으로 붙여 실행마다 JobInstance 를 새로 만든다.
 * - `triggerType` · `scheduleKey` · `requestedBy` 는 기록용이라 non-identifying 이다.
 */
@Component
class SpringBatchJobAdapter(
    jobs: List<Job>,
    private val jobOperator: JobOperator,
    private val jobRepository: JobRepository,
) : JobLaunchPort {

    private val jobsByName: Map<String, Job> =
        jobs.associateBy {
            it.name
        }

    override fun exists(jobName: String): Boolean =
        jobName in jobsByName

    override fun isRunning(jobName: String, parameters: Map<String, String>): Boolean =
        jobRepository.findRunningJobExecutions(jobName).any {
            businessParameters(it.jobParameters) == parameters
        }

    override fun launch(request: JobLaunchRequest): Long {
        val job = requireNotNull(jobsByName[request.jobName]) {
            "등록되지 않은 Job 입니다: ${request.jobName}"
        }
        return jobOperator.start(job, toJobParameters(request)).id
    }

    override fun recoverInterrupted(): List<Long> =
        jobsByName.keys.flatMap { jobName ->
            jobRepository.findRunningJobExecutions(jobName).map {
                jobOperator.recover(it).id
            }
        }

    private fun toJobParameters(request: JobLaunchRequest): JobParameters {
        val builder = JobParametersBuilder()
        request.parameters.forEach { (name, value) ->
            builder.addString(name, value)
        }

        when (val trigger = request.trigger) {
            is JobTrigger.Scheduled -> builder
                .addLocalDateTime(BatchJobParameters.SCHEDULED_AT, trigger.scheduledAt)
                .addString(BatchJobParameters.TRIGGER_TYPE, "SCHEDULED", false)
                .addString(BatchJobParameters.SCHEDULE_KEY, trigger.scheduleKey, false)
            is JobTrigger.Manual -> builder
                .addLong(BatchJobParameters.REQUEST_ID, trigger.requestId)
                .addString(BatchJobParameters.TRIGGER_TYPE, "MANUAL", false)
                .addString(BatchJobParameters.REQUESTED_BY, trigger.requestedBy, false)
        }
        return builder.toJobParameters()
    }

    private fun businessParameters(jobParameters: JobParameters): Map<String, String> =
        jobParameters.parameters()
            .filter {
                it.identifying() && it.name() !in BatchJobParameters.RESERVED_NAMES
            }
            .associate {
                it.name() to it.value().toString()
            }

}
