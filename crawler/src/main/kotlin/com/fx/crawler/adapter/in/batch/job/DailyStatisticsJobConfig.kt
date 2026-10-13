package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJob
import com.fx.common.domain.batch.BatchJobParameter
import com.fx.crawler.application.port.`in`.StatisticsAggregationUseCase
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
 * 일일 통계 Job. 최근 `lookbackDays` 일을 매번 다시 집계해 놓친 날을 스스로 메우고,
 * 집계가 끝난 뒤에 `apiLogRetentionDays` 일이 지난 API 로그를 지운다.
 *
 * 폴러가 실행마다 새 JobInstance 를 만들므로, 다시 실행해도 결과가 같은 것은 통계 테이블의 덮어쓰기 upsert 가 맡는다.
 * 집계는 날짜 하나, 삭제는 [DELETE_LIMIT] 개마다 트랜잭션을 나눠 하나가 커지지 않게 한다.
 */
@Configuration(proxyBeanMethods = false)
class DailyStatisticsJobConfig(
    private val jobRepository: JobRepository,
) {

    /** 집계 Step 이 실패하면 Job 이 멈추므로, 집계하지 못한 날짜의 원본은 지우지 않는다. */
    @Bean
    fun dailyStatisticsJob(
        @Qualifier("dailyStatisticsAggregateStep") dailyStatisticsAggregateStep: Step,
        @Qualifier("apiLogPurgeStep") apiLogPurgeStep: Step,
    ): Job =
        JobBuilder(BatchJob.DAILY_STATISTICS.jobName, jobRepository)
            .start(dailyStatisticsAggregateStep)
            .next(apiLogPurgeStep)
            .build()

    @Bean
    fun dailyStatisticsAggregateStep(
        @Qualifier("dailyStatisticsAggregateTasklet") dailyStatisticsAggregateTasklet: Tasklet,
        transactionManager: PlatformTransactionManager,
    ): Step =
        StepBuilder("dailyStatisticsAggregateStep", jobRepository)
            .tasklet(dailyStatisticsAggregateTasklet, transactionManager)
            .build()

    @Bean
    fun apiLogPurgeStep(
        @Qualifier("apiLogPurgeTasklet") apiLogPurgeTasklet: Tasklet,
        transactionManager: PlatformTransactionManager,
    ): Step =
        StepBuilder("apiLogPurgeStep", jobRepository)
            .tasklet(apiLogPurgeTasklet, transactionManager)
            .build()

    /** 반복 한 번에 날짜 하나를 오래된 날짜부터 집계한다. */
    @Bean
    @StepScope
    fun dailyStatisticsAggregateTasklet(
        @Value("#{jobParameters['lookbackDays']}") lookbackDays: String?,
        @Value("#{jobParameters['apiLogRetentionDays']}") apiLogRetentionDays: String?,
        statisticsAggregationUseCase: StatisticsAggregationUseCase,
    ): Tasklet {
        val days = requireNotNull(lookbackDays?.toIntOrNull()) {
            "lookbackDays 파라미터(다시 집계할 일수)가 필요합니다: $lookbackDays"
        }
        val retentionDays = requireNotNull(apiLogRetentionDays?.toIntOrNull()) {
            "apiLogRetentionDays 파라미터(API 로그 보존 일수)가 필요합니다: $apiLogRetentionDays"
        }
        // 폴러를 거치지 않은 실행도 집계 전에 막는다. 로그가 지워지기 시작한 날짜를 다시 집계하면 통계가 줄어든다
        BatchJob.DAILY_STATISTICS.validate(
            mapOf(
                BatchJobParameter.LOOKBACK_DAYS.parameterName to days.toString(),
                BatchJobParameter.API_LOG_RETENTION_DAYS.parameterName to retentionDays.toString(),
            )
        )
        // Step 이 시작할 때 오늘을 한 번만 정한다 (자정을 넘겨 실행돼도 대상 날짜가 바뀌지 않도록)
        val plan =
            statisticsAggregationUseCase.plan(days)
        val remaining =
            ArrayDeque(plan.dates)

        return Tasklet { contribution, _ ->
            val date = remaining.removeFirstOrNull()
                ?: return@Tasklet RepeatStatus.FINISHED
            val written =
                statisticsAggregationUseCase.aggregate(date, captureSnapshot = date == plan.snapshotDate)
            contribution.incrementWriteCount(written.toLong())
            if (remaining.isEmpty()) RepeatStatus.FINISHED else RepeatStatus.CONTINUABLE
        }
    }

    @Bean
    @StepScope
    fun apiLogPurgeTasklet(
        @Value("#{jobParameters['apiLogRetentionDays']}") apiLogRetentionDays: String?,
        statisticsAggregationUseCase: StatisticsAggregationUseCase,
    ): Tasklet {
        val days = requireNotNull(apiLogRetentionDays?.toIntOrNull()) {
            "apiLogRetentionDays 파라미터(API 로그 보존 일수)가 필요합니다: $apiLogRetentionDays"
        }

        return Tasklet { contribution, _ ->
            val deleted =
                statisticsAggregationUseCase.deleteExpiredApiLogs(days, DELETE_LIMIT)
            contribution.incrementWriteCount(deleted.toLong())
            if (deleted > 0) RepeatStatus.CONTINUABLE else RepeatStatus.FINISHED
        }
    }

    companion object {
        const val DELETE_LIMIT = 5_000
    }

}
