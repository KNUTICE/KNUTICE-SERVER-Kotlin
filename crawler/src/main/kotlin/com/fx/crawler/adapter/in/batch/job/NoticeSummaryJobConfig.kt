package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJobNames
import com.fx.crawler.adapter.`in`.batch.support.CatalogRefreshJobListener
import com.fx.crawler.adapter.`in`.batch.support.KeysetItemReader
import com.fx.crawler.adapter.`in`.batch.support.StepTransactions
import com.fx.crawler.application.port.`in`.NoticeSummaryUseCase
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 공지 AI 요약 Job. 공지 · 학과 게시판의 요약 대기 공지를 요약한다.
 *
 * - 크롤링 Job 과 따로 돌아 요약이 길어져도 크롤링 · 발송이 밀리지 않는다. 스케줄을 끄면 요약만 멈추고, 그동안의 공지는 대기로 남는다.
 * - 실패는 시도 횟수로 남기고, 재시도 간격이 지난 뒤 다음 실행에서 다시 시도한다.
 * - 호출 한도에 걸린 공지는 상태를 바꾸지 않고 다음 실행으로 미룬다.
 */
@Configuration(proxyBeanMethods = false)
class NoticeSummaryJobConfig(
    private val jobRepository: JobRepository,
    private val properties: CrawlerProperties,
) {

    @Bean
    fun noticeSummaryJob(
        catalogRefreshJobListener: CatalogRefreshJobListener,
        @Qualifier("noticeSummaryStep") noticeSummaryStep: Step,
    ): Job =
        JobBuilder(BatchJobNames.NOTICE_SUMMARY, jobRepository)
            .listener(catalogRefreshJobListener)
            .start(noticeSummaryStep)
            .build()

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
    fun noticeSummaryReader(noticeSummaryUseCase: NoticeSummaryUseCase): ItemStreamReader<SummaryTarget> =
        KeysetItemReader("noticeSummary", properties.summary.chunkSize, SummaryTarget::noticeId) { afterKey, size ->
            noticeSummaryUseCase.findTargets(afterKey, size)
        }

}
