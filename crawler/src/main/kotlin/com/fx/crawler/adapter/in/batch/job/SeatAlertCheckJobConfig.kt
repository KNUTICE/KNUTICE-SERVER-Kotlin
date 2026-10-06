package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJob
import com.fx.crawler.adapter.`in`.batch.support.CatalogRefreshJobListener
import com.fx.crawler.adapter.`in`.batch.support.StepTransactions
import com.fx.crawler.application.port.`in`.SeatAlertCheckUseCase
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 열람실 빈자리 확인 Job (1분마다).
 *
 * 1. 만료 정리 : 만료된 알림을 지운다
 * 2. 확인 · 발송 : 만료되지 않은 알림의 열람실 좌석을 조회해 빈 좌석이면 알림을 보내고 지운다
 */
@Configuration(proxyBeanMethods = false)
class SeatAlertCheckJobConfig(
    private val jobRepository: JobRepository,
) {

    @Bean
    fun seatAlertCheckJob(
        catalogRefreshJobListener: CatalogRefreshJobListener,
        @Qualifier("seatAlertExpireStep") seatAlertExpireStep: Step,
        @Qualifier("seatAlertNotifyStep") seatAlertNotifyStep: Step,
    ): Job =
        JobBuilder(BatchJob.SEAT_ALERT_CHECK.jobName, jobRepository)
            .listener(catalogRefreshJobListener)
            .start(seatAlertExpireStep)
            .next(seatAlertNotifyStep)
            .build()

    @Bean
    fun seatAlertExpireStep(seatAlertCheckUseCase: SeatAlertCheckUseCase): Step =
        StepBuilder("seatAlertExpireStep", jobRepository)
            .tasklet({ contribution, _ ->
                contribution.incrementWriteCount(seatAlertCheckUseCase.deleteExpired().toLong())
                RepeatStatus.FINISHED
            }, StepTransactions.NONE)
            .build()

    @Bean
    fun seatAlertNotifyStep(seatAlertCheckUseCase: SeatAlertCheckUseCase): Step =
        StepBuilder("seatAlertNotifyStep", jobRepository)
            .tasklet({ contribution, _ ->
                contribution.incrementWriteCount(seatAlertCheckUseCase.checkAndNotify().toLong())
                RepeatStatus.FINISHED
            }, StepTransactions.NONE)
            .build()

}
