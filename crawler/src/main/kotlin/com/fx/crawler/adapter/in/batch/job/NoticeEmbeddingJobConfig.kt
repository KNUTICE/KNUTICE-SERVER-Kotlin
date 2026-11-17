package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJob
import com.fx.crawler.adapter.`in`.batch.support.CatalogRefreshJobListener
import com.fx.crawler.adapter.`in`.batch.support.KeysetItemReader
import com.fx.crawler.adapter.`in`.batch.support.StepTransactions
import com.fx.crawler.application.port.`in`.NoticeEmbeddingUseCase
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.embedding.EmbeddingResult
import com.fx.crawler.domain.embedding.EmbeddingTarget
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
 * 공지 RAG 임베딩 Job. 임베딩 대기 공지를 최신 게시글부터 청크로 나눠 임베딩한다.
 *
 * - 요약 Job 과 같은 구조다. 크롤링 · 요약과 따로 돌고, 스케줄을 끄면 임베딩만 멈춘 채 공지는 대기로 남는다.
 * - 실패는 시도 횟수로 남기고, 재시도 간격이 지난 뒤 다음 실행에서 다시 시도한다.
 * - 호출 한도에 걸린 공지는 상태를 바꾸지 않고 다음 실행으로 미룬다. 무료 한도(하루 1,000회)를 다 쓰면 다음 날 이어서 한다.
 */
@Configuration(proxyBeanMethods = false)
class NoticeEmbeddingJobConfig(
    private val jobRepository: JobRepository,
    private val properties: CrawlerProperties,
) {

    @Bean
    fun noticeEmbeddingJob(
        catalogRefreshJobListener: CatalogRefreshJobListener,
        @Qualifier("noticeEmbeddingStep") noticeEmbeddingStep: Step,
    ): Job =
        JobBuilder(BatchJob.NOTICE_EMBEDDING.jobName, jobRepository)
            .listener(catalogRefreshJobListener)
            .start(noticeEmbeddingStep)
            .build()

    /** 학교 사이트 · 임베딩 호출이 도는 동안 DB 커넥션을 잡지 않도록 트랜잭션 없이 읽고, 결과 반영만 트랜잭션으로 묶는다. */
    @Bean
    fun noticeEmbeddingStep(
        @Qualifier("noticeEmbeddingReader") noticeEmbeddingReader: ItemStreamReader<EmbeddingTarget>,
        noticeEmbeddingUseCase: NoticeEmbeddingUseCase,
    ): Step =
        StepBuilder("noticeEmbeddingStep", jobRepository)
            .chunk<EmbeddingTarget, EmbeddingResult>(properties.embedding.chunkSize)
            .transactionManager(StepTransactions.NONE)
            .reader(noticeEmbeddingReader)
            .processor(ItemProcessor { target ->
                noticeEmbeddingUseCase.embed(target)
            })
            .writer(ItemWriter { chunk ->
                noticeEmbeddingUseCase.applyResults(chunk.items)
            })
            .build()

    /** 게시글 번호 내림차순 keyset. 다음 페이지는 마지막으로 읽은 번호보다 작은 공지다. */
    @Bean
    @StepScope
    fun noticeEmbeddingReader(noticeEmbeddingUseCase: NoticeEmbeddingUseCase): ItemStreamReader<EmbeddingTarget> =
        KeysetItemReader("noticeEmbedding", properties.embedding.chunkSize, EmbeddingTarget::nttId) { beforeNttId, size ->
            noticeEmbeddingUseCase.findTargets(beforeNttId, size)
        }

}
