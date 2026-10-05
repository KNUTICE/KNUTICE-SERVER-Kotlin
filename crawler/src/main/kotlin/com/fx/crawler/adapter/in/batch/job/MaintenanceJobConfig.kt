package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJobNames
import com.fx.crawler.application.port.`in`.BatchMaintenanceUseCase
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * Spring Batch 메타데이터 정리 Job. 파라미터 `retentionDays` 일보다 오래전에 끝난 실행 기록을 지운다.
 * 한 번에 [DELETE_LIMIT] 개씩 지우고 반복하므로 트랜잭션 하나가 커지지 않는다.
 */
@Configuration(proxyBeanMethods = false)
class MaintenanceJobConfig(
    private val jobRepository: JobRepository,
) {

    @Bean
    fun maintenanceJob(@Qualifier("maintenanceStep") maintenanceStep: Step): Job =
        JobBuilder(BatchJobNames.MAINTENANCE, jobRepository)
            .start(maintenanceStep)
            .build()

    @Bean
    fun maintenanceStep(
        @Qualifier("maintenanceTasklet") maintenanceTasklet: Tasklet,
        transactionManager: PlatformTransactionManager,
    ): Step =
        StepBuilder("maintenanceStep", jobRepository)
            .tasklet(maintenanceTasklet, transactionManager)
            .build()

    /** 오래된 JobExecution 을 모두 지운 뒤, 실행 기록이 남지 않은 JobInstance 를 지운다. */
    @Bean
    @StepScope
    fun maintenanceTasklet(
        @Value("#{jobParameters['retentionDays']}") retentionDays: String?,
        batchMaintenanceUseCase: BatchMaintenanceUseCase,
    ): Tasklet {
        val days = requireNotNull(retentionDays?.toIntOrNull()) {
            "retentionDays 파라미터(보존 일수)가 필요합니다: $retentionDays"
        }
        var executionsDone = false

        return Tasklet { contribution, _ ->
            if (!executionsDone) {
                val deletedExecutions = batchMaintenanceUseCase.deleteExpiredExecutions(days, DELETE_LIMIT)
                if (deletedExecutions > 0) {
                    contribution.incrementWriteCount(deletedExecutions.toLong())
                    return@Tasklet RepeatStatus.CONTINUABLE
                }
                executionsDone = true
            }
            val deletedInstances = batchMaintenanceUseCase.deleteOrphanInstances(DELETE_LIMIT)
            if (deletedInstances > 0) RepeatStatus.CONTINUABLE else RepeatStatus.FINISHED
        }
    }

    companion object {
        const val DELETE_LIMIT = 500
    }

}
