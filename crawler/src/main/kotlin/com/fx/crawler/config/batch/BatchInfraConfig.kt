package com.fx.crawler.config.batch

import com.fx.common.concurrent.BoundedParallelExecutor
import com.fx.crawler.config.CrawlerProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor

@Configuration(proxyBeanMethods = false)
class BatchInfraConfig {

    /** JobOperator 가 Job 을 이 실행기로 시작한다. 폴러는 Job 이 끝나기를 기다리지 않는다. */
    @Bean
    @BatchTaskExecutor
    @ConditionalOnProperty(prefix = "crawler.launch", name = ["async"], havingValue = "true", matchIfMissing = true)
    fun batchTaskExecutor(properties: CrawlerProperties): TaskExecutor =
        SimpleAsyncTaskExecutor("batch-").apply {
            setVirtualThreads(true)
            concurrencyLimit = properties.launch.maxConcurrency
        }

    /** 토픽별 발송 파티션 실행기. 모든 발송 Job 이 함께 쓰므로 동시 발송 토픽 수의 상한이 된다. */
    @Bean
    fun pushPartitionTaskExecutor(properties: CrawlerProperties): TaskExecutor =
        SimpleAsyncTaskExecutor("push-").apply {
            setVirtualThreads(true)
            concurrencyLimit = properties.push.partitionConcurrency
        }

    /** 학교 사이트(게시판 · 식단) 요청의 동시 실행 상한. */
    @Bean
    fun schoolSiteExecutor(properties: CrawlerProperties): BoundedParallelExecutor =
        BoundedParallelExecutor(properties.crawl.maxConcurrency)

    /** 열람실 사이트 요청의 동시 실행 상한. */
    @Bean
    fun readingRoomSiteExecutor(properties: CrawlerProperties): BoundedParallelExecutor =
        BoundedParallelExecutor(properties.seatAlert.maxConcurrency)

}
