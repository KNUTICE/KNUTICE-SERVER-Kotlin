package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.domain.notice.SummaryStatus
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.application.port.out.NoticeSummaryPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget
import com.fx.crawler.fixture.CrawlerFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoticeSummaryServiceTest {

    private val noticePersistencePort = mockk<NoticePersistencePort>()
    private val noticeSummaryPort = mockk<NoticeSummaryPort>()
    private val webhookPort = mockk<WebhookPort>(relaxed = true)
    private val service = NoticeSummaryService(
        mockk<CatalogQueryUseCase>(),
        noticePersistencePort,
        noticeSummaryPort,
        webhookPort,
        CrawlerProperties(summary = CrawlerProperties.Summary(maxAttempts = 2)),
    )

    private fun target(content: String?) = SummaryTarget(noticeId = 10, nttId = 1, topicCode = 1, title = "공지", content = content)

    @Test
    fun `본문이 없으면 요약하지 않는다`() {
        assertThat(service.summarize(target(" "))).isEqualTo(SummaryResult.Skipped(10))
        verify(exactly = 0) { noticeSummaryPort.summarize(any()) }
    }

    @Test
    fun `요약 실패는 예외 대신 결과로 돌려준다`() {
        every { noticeSummaryPort.summarize("본문") } throws IllegalStateException("429 quota")

        assertThat(service.summarize(target("본문"))).isEqualTo(SummaryResult.Failed(10, "429 quota"))
    }

    @Test
    fun `결과에 따라 요약을 저장하고 상태를 바꾼다`() {
        val completed = CrawlerFixture.notice(1)
        val skipped = CrawlerFixture.notice(2)
        val failed = CrawlerFixture.notice(3)
        val content = NoticeContent(10, content = "본문", contentSummary = null)
        every { noticePersistencePort.findAllByIds(any()) } returns listOf(completed, skipped, failed)
        every { noticePersistencePort.findContents(any()) } returns listOf(content)

        service.applyResults(
            listOf(SummaryResult.Completed(10, "요약"), SummaryResult.Skipped(20), SummaryResult.Failed(30, "오류"))
        )

        assertThat(content.contentSummary).isEqualTo("요약")
        assertThat(completed.summaryStatus).isEqualTo(SummaryStatus.COMPLETED)
        assertThat(skipped.summaryStatus).isEqualTo(SummaryStatus.SKIPPED)
        // 한도(2회) 전이므로 다음 실행에서 다시 시도한다
        assertThat(failed.summaryStatus).isEqualTo(SummaryStatus.PENDING)
        assertThat(failed.summaryAttemptCount).isEqualTo(1)
        verify(exactly = 0) { webhookPort.notifySlack(any()) }
    }

    @Test
    fun `시도 한도에 닿으면 FAILED 로 두고 Slack 으로 알린다`() {
        val failed = CrawlerFixture.notice(3).apply { failSummary(maxAttempts = 2) }
        every { noticePersistencePort.findAllByIds(any()) } returns listOf(failed)
        every { noticePersistencePort.findContents(any()) } returns emptyList()

        service.applyResults(listOf(SummaryResult.Failed(30, "오류")))

        assertThat(failed.summaryStatus).isEqualTo(SummaryStatus.FAILED)
        verify(exactly = 1) { webhookPort.notifySlack(match { it.content.contains("[nttId : 3]") }) }
    }

}
