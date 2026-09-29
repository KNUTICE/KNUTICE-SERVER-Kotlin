package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJobNames
import com.fx.crawler.adapter.`in`.batch.support.KeysetItemReader
import com.fx.crawler.application.port.`in`.PushSendUseCase
import com.fx.crawler.application.port.`in`.PushTargetQueryUseCase
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.push.PushTarget
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/** 활성 iOS 토큰 전체에 사일런트 푸시를 보내 앱이 토큰을 다시 등록하게 한다. */
@Configuration(proxyBeanMethods = false)
class SilentPushJobConfig(
    private val jobRepository: JobRepository,
    private val properties: CrawlerProperties,
) {

    @Bean
    fun silentPushJob(@Qualifier("silentPushStep") silentPushStep: Step): Job =
        JobBuilder(BatchJobNames.SILENT_PUSH, jobRepository)
            .start(silentPushStep)
            .build()

    @Bean
    fun silentPushStep(
        @Qualifier("silentPushReader") silentPushReader: ItemStreamReader<PushTarget>,
        pushSendUseCase: PushSendUseCase,
        transactionManager: PlatformTransactionManager,
    ): Step =
        StepBuilder("silentPushStep", jobRepository)
            .chunk<PushTarget, PushTarget>(properties.push.chunkSize)
            .transactionManager(transactionManager)
            .reader(silentPushReader)
            .writer(ItemWriter { chunk -> pushSendUseCase.sendSilent(chunk.items) })
            .build()

    @Bean
    @StepScope
    fun silentPushReader(pushTargetQueryUseCase: PushTargetQueryUseCase): ItemStreamReader<PushTarget> =
        KeysetItemReader("silentPush", properties.push.chunkSize, PushTarget::fcmTokenId) { afterKey, size ->
            pushTargetQueryUseCase.findActiveIosTargets(afterKey, size)
        }

}
