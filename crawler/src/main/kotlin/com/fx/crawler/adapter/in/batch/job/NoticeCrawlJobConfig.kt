package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.TopicType
import com.fx.common.domain.batch.BatchJobNames
import com.fx.crawler.adapter.`in`.batch.push.PushPartitionStepFactory
import com.fx.crawler.adapter.`in`.batch.push.TopicPushPartitioner
import com.fx.crawler.adapter.`in`.batch.support.CatalogRefreshJobListener
import com.fx.crawler.adapter.`in`.batch.support.KeysetItemReader
import com.fx.crawler.adapter.`in`.batch.support.StepTransactions
import com.fx.crawler.application.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.application.port.`in`.NoticePushUseCase
import com.fx.crawler.application.port.`in`.NoticeSummaryUseCase
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

/**
 * 공지 크롤링 Job. 파라미터 `topicType` (NOTICE / MAJOR) 게시판을 대상으로 한다.
 *
 * 1. 크롤링 : 목록 → 신규 판별 → 상세 → 저장 (알림 · 요약 대기)
 * 2. 발송 : 발송 대기 공지를 토픽별 파티션으로 나눠 구독자에게 보내고, 토픽이 끝나면 그 공지를 발송 완료로 표시한다
 * 3. AI 요약 : 요약 대기 공지를 요약한다. 실패는 시도 횟수로 남겨 다음 실행에서 다시 시도한다
 */
@Configuration(proxyBeanMethods = false)
class NoticeCrawlJobConfig(
    private val jobRepository: JobRepository,
    private val properties: CrawlerProperties,
) {

    @Bean
    fun noticeCrawlJob(
        catalogRefreshJobListener: CatalogRefreshJobListener,
        @Qualifier("noticeCrawlStep") noticeCrawlStep: Step,
        @Qualifier("noticePushStep") noticePushStep: Step,
        @Qualifier("noticeSummaryStep") noticeSummaryStep: Step,
    ): Job =
        JobBuilder(BatchJobNames.NOTICE_CRAWL, jobRepository)
            .listener(catalogRefreshJobListener)
            .start(noticeCrawlStep)
            .next(noticePushStep)
            .next(noticeSummaryStep)
            .build()

    @Bean
    fun noticeCrawlStep(@Qualifier("noticeCrawlTasklet") noticeCrawlTasklet: Tasklet): Step =
        StepBuilder("noticeCrawlStep", jobRepository)
            .tasklet(noticeCrawlTasklet, StepTransactions.NONE)
            .build()

    @Bean
    @StepScope
    fun noticeCrawlTasklet(
        @Value("#{jobParameters['topicType']}") topicType: String,
        noticeCrawlUseCase: NoticeCrawlUseCase,
    ): Tasklet =
        Tasklet { contribution, _ ->
            contribution.incrementWriteCount(noticeCrawlUseCase.crawlAndSave(TopicType.valueOf(topicType)).toLong())
            RepeatStatus.FINISHED
        }

    @Bean
    fun noticePushStep(
        pushPartitionStepFactory: PushPartitionStepFactory,
        @Qualifier("noticePushPartitioner") noticePushPartitioner: Partitioner,
    ): Step =
        pushPartitionStepFactory.create("noticePushStep", noticePushPartitioner)

    @Bean
    @JobScope
    fun noticePushPartitioner(
        @Value("#{jobParameters['topicType']}") topicType: String,
        noticePushUseCase: NoticePushUseCase,
        jsonMapper: JsonMapper,
    ): Partitioner =
        TopicPushPartitioner(jsonMapper) {
            noticePushUseCase.preparePushPlans(TopicType.valueOf(topicType))
        }

    /** 요약 호출이 도는 동안 DB 커넥션을 잡지 않도록 트랜잭션 없이 읽고, 결과 반영만 트랜잭션으로 묶는다. */
    @Bean
    fun noticeSummaryStep(
        @Qualifier("noticeSummaryReader") noticeSummaryReader: ItemStreamReader<SummaryTarget>,
        noticeSummaryUseCase: NoticeSummaryUseCase,
    ): Step =
        StepBuilder("noticeSummaryStep", jobRepository)
            .chunk<SummaryTarget, SummaryResult>(properties.summary.chunkSize)
            .transactionManager(StepTransactions.NONE)
            .reader(noticeSummaryReader)
            .processor(ItemProcessor { target ->
                noticeSummaryUseCase.summarize(target)
            })
            .writer(ItemWriter { chunk ->
                noticeSummaryUseCase.applyResults(chunk.items)
            })
            .build()

    @Bean
    @StepScope
    fun noticeSummaryReader(
        @Value("#{jobParameters['topicType']}") topicType: String,
        noticeSummaryUseCase: NoticeSummaryUseCase,
    ): ItemStreamReader<SummaryTarget> =
        KeysetItemReader("noticeSummary", properties.summary.chunkSize, SummaryTarget::noticeId) { afterKey, size ->
            noticeSummaryUseCase.findTargets(TopicType.valueOf(topicType), afterKey, size)
        }

}
