package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.adapter.out.persistence.repository.NoticeContentRepository
import com.fx.common.adapter.out.persistence.repository.NoticeRepository
import com.fx.common.domain.SlackType
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.domain.notice.SummaryStatus
import com.fx.crawler.support.CrawlerIntegrationTest
import com.fx.crawler.support.FakeNoticeCrawlPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDate
import java.time.LocalDateTime

class NoticeSummaryJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("noticeSummaryJob") lateinit var noticeSummaryJob: Job
    @Autowired lateinit var noticeRepository: NoticeRepository
    @Autowired lateinit var noticeContentRepository: NoticeContentRepository

    private val generalNews = topic(1, "GENERAL_NEWS", TopicType.NOTICE)
    private val mechanicalEngineering = topic(100, "MECHANICAL_ENGINEERING", TopicType.MAJOR)

    @Test
    fun `공지와 학과 게시판의 요약 대기 공지를 요약한다`() {
        saveNotice(100, generalNews, content = "본문 100")
        saveNotice(300, mechanicalEngineering, content = "본문 300")
        saveNotice(101, generalNews, content = null)
        saveNotice(102, generalNews, content = "본문 102")
        noticeSummaryPort.failingContents += "본문 102"

        val execution = run(noticeSummaryJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(statusOf(100)).isEqualTo(SummaryStatus.COMPLETED)
        assertThat(summaryOf(100)).isEqualTo("요약: 본문 100")
        assertThat(statusOf(300)).isEqualTo(SummaryStatus.COMPLETED)
        assertThat(statusOf(101)).isEqualTo(SummaryStatus.SKIPPED)
        // 실패는 시도 횟수만 늘리고 대기로 남긴다
        assertThat(statusOf(102)).isEqualTo(SummaryStatus.PENDING)
        assertThat(noticeRepository.findByNttId(102)!!.summaryAttemptCount).isEqualTo(1)
    }

    @Test
    fun `실패한 공지는 재시도 간격이 지난 뒤에만 다시 요약한다`() {
        saveNotice(200, generalNews, content = "본문 200", failedAttempts = 1)
        saveNotice(201, generalNews, content = "본문 201", failedAttempts = 1)
        jdbcTemplate.update("UPDATE notice SET updated_at = ? WHERE ntt_id = 201", LocalDateTime.now().minusMinutes(31))

        val execution = run(noticeSummaryJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        // 방금 실패한 공지는 건너뛴다
        assertThat(statusOf(200)).isEqualTo(SummaryStatus.PENDING)
        assertThat(summaryOf(200)).isNull()
        assertThat(statusOf(201)).isEqualTo(SummaryStatus.COMPLETED)
    }

    @Test
    fun `호출 한도에 걸리면 상태와 시도 횟수를 그대로 두고 다음 실행으로 미룬다`() {
        saveNotice(100, generalNews, content = "본문 100")
        noticeSummaryPort.rateLimited = true

        val execution = run(noticeSummaryJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        val notice = noticeRepository.findByNttId(100)!!
        assertThat(notice.summaryStatus).isEqualTo(SummaryStatus.PENDING)
        assertThat(notice.summaryAttemptCount).isZero()
        assertThat(webhookPort.messages.filter {
            it.type == SlackType.AI_ERROR
        }).isEmpty()

        noticeSummaryPort.rateLimited = false
        run(noticeSummaryJob)

        assertThat(statusOf(100)).isEqualTo(SummaryStatus.COMPLETED)
    }

    private fun saveNotice(nttId: Long, topic: TopicView, content: String?, failedAttempts: Int = 0) {
        val notice = Notice.crawled(
            nttId = nttId,
            topic = topic,
            title = "공지 $nttId",
            department = "학사팀",
            contentUrl = FakeNoticeCrawlPort.contentUrl(nttId),
            contentImageUrl = null,
            registrationDate = LocalDate.of(2026, 9, 29),
            isAttachment = false,
        )
        repeat(failedAttempts) {
            notice.failSummary(maxAttempts = 3)
        }
        val saved = noticeRepository.save(notice)
        noticeContentRepository.save(NoticeContent(requireNotNull(saved.id), content, null))
    }

    private fun statusOf(nttId: Long): SummaryStatus =
        noticeRepository.findByNttId(nttId)!!.summaryStatus

    private fun summaryOf(nttId: Long): String? =
        noticeContentRepository.findByNoticeId(requireNotNull(noticeRepository.findByNttId(nttId)!!.id))?.contentSummary

    private fun topic(code: Int, name: String, type: TopicType) =
        TopicView(code.toLong(), code, name, type, LocalizedText(name), null, "https://www.ut.ac.kr", "/", true, true)

}
