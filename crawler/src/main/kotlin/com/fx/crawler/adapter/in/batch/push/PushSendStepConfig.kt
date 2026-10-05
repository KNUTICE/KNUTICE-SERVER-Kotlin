package com.fx.crawler.adapter.`in`.batch.push

import com.fx.crawler.adapter.`in`.batch.support.KeysetItemReader
import com.fx.crawler.application.port.`in`.PushSendUseCase
import com.fx.crawler.application.port.`in`.PushTargetQueryUseCase
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.push.LocalizedPushMessages
import com.fx.crawler.domain.push.PushTarget
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

/**
 * 공지 · 학식이 함께 쓰는 발송 Step.
 *
 * 각 Job 은 [PushPartitionStepFactory] 로 자기 파티셔너를 붙인 파티션 Step 을 만든다.
 * 파티션(토픽) 하나는 구독자를 토큰 id 순으로 chunk 단위로 읽어, 토큰 언어별로 알림을 보낸다.
 * FCM 발송은 되돌릴 수 없으므로 chunk 재시도를 걸지 않는다.
 */
@Configuration(proxyBeanMethods = false)
class PushSendStepConfig(
    private val jobRepository: JobRepository,
    private val properties: CrawlerProperties,
) {

    @Bean
    fun pushSendWorkerStep(
        @Qualifier("pushSubscriberReader") pushSubscriberReader: ItemStreamReader<PushTarget>,
        @Qualifier("pushSendWriter") pushSendWriter: ItemWriter<PushTarget>,
        noticeNotifiedListener: NoticeNotifiedListener,
        transactionManager: PlatformTransactionManager,
    ): Step =
        StepBuilder(WORKER_STEP_NAME, jobRepository)
            .chunk<PushTarget, PushTarget>(properties.push.chunkSize)
            .transactionManager(transactionManager)
            .reader(pushSubscriberReader)
            .writer(pushSendWriter)
            .listener(noticeNotifiedListener)
            .build()

    @Bean
    @StepScope
    fun pushSubscriberReader(
        @Value("#{stepExecutionContext['topicCode']}") topicCode: Int,
        pushTargetQueryUseCase: PushTargetQueryUseCase,
    ): ItemStreamReader<PushTarget> =
        KeysetItemReader("pushSubscriber", properties.push.chunkSize, PushTarget::fcmTokenId) { afterKey, size ->
            pushTargetQueryUseCase.findSubscribers(topicCode, afterKey, size)
        }

    @Bean
    @StepScope
    fun pushSendWriter(
        @Value("#{stepExecutionContext['messages']}") messagesJson: String,
        pushSendUseCase: PushSendUseCase,
        jsonMapper: JsonMapper,
    ): ItemWriter<PushTarget> {
        val messages = jsonMapper.readValue<LocalizedPushMessages>(messagesJson)
        return ItemWriter { chunk ->
            pushSendUseCase.send(chunk.items, messages)
        }
    }

    @Bean
    fun pushPartitionStepFactory(
        @Qualifier("pushSendWorkerStep") pushSendWorkerStep: Step,
        @Qualifier("pushPartitionTaskExecutor") pushPartitionTaskExecutor: TaskExecutor,
    ): PushPartitionStepFactory =
        PushPartitionStepFactory(jobRepository, pushSendWorkerStep, pushPartitionTaskExecutor)

    companion object {
        const val WORKER_STEP_NAME = "pushSendWorkerStep"
    }

}

/** 파티셔너가 나눈 토픽마다 발송 Step 을 병렬(동시 실행 상한 안에서)로 실행하는 파티션 Step 을 만든다. */
class PushPartitionStepFactory(
    private val jobRepository: JobRepository,
    private val workerStep: Step,
    private val taskExecutor: TaskExecutor,
) {

    fun create(stepName: String, partitioner: Partitioner): Step =
        StepBuilder(stepName, jobRepository)
            .partitioner(PushSendStepConfig.WORKER_STEP_NAME, partitioner)
            .step(workerStep)
            .taskExecutor(taskExecutor)
            .build()

}
