package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.adapter.out.persistence.repository.FcmTokenRepository
import com.fx.common.adapter.out.persistence.repository.FcmTokenSubscriptionRepository
import com.fx.common.adapter.out.persistence.repository.NoticeContentRepository
import com.fx.common.adapter.out.persistence.repository.NoticeRepository
import com.fx.common.domain.DeviceType
import com.fx.common.domain.SlackType
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.domain.fcmtoken.FcmTokenSubscription
import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.domain.notice.NotificationStatus
import com.fx.common.domain.notice.SummaryStatus
import com.fx.crawler.domain.crawl.NoticeDetail
import com.fx.crawler.support.CrawlerIntegrationTest
import com.fx.crawler.support.FakeNoticeCrawlPort
import com.fx.crawler.support.FakeNoticeCrawlPort.Row
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDate

class NoticeCrawlJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("noticeCrawlJob") lateinit var noticeCrawlJob: Job
    @Autowired lateinit var noticeRepository: NoticeRepository
    @Autowired lateinit var noticeContentRepository: NoticeContentRepository
    @Autowired lateinit var fcmTokenRepository: FcmTokenRepository
    @Autowired lateinit var fcmTokenSubscriptionRepository: FcmTokenSubscriptionRepository

    private val generalNews = topic(1, "GENERAL_NEWS")
    private val scholarshipNews = topic(2, "SCHOLARSHIP_NEWS")

    @AfterEach
    fun restoreTranslations() {
        jdbcTemplate.update("UPDATE notification_template SET text_ja = NULL WHERE template_key = 'NOTICE_BODY_MULTIPLE'")
        jdbcTemplate.update("UPDATE topic SET display_name_ja = NULL WHERE code = 1")
    }

    @Test
    fun `새 공지를 저장하고 토픽별로 언어에 맞춰 발송한 뒤 요약한다`() {
        jdbcTemplate.update("UPDATE notification_template SET text_ja = '{title} ほか{count}件' WHERE template_key = 'NOTICE_BODY_MULTIPLE'")
        jdbcTemplate.update("UPDATE topic SET display_name_ja = '一般ニュース' WHERE code = 1")

        // 이전 실행이 발송 전에 멈춰 남은 공지와 이미 보낸 공지
        saveNotice(100, generalNews, NotificationStatus.PENDING, content = "본문 100")
        saveNotice(99, generalNews, NotificationStatus.SENT, content = null)

        noticeCrawlPort.lists["GENERAL_NEWS"] = listOf(Row(102, "공지 102"), Row(101, "공지 101"), Row(100, "공지 100"), Row(99, "공지 99"))
        noticeCrawlPort.lists["SCHOLARSHIP_NEWS"] = listOf(Row(200, "장학 200"))
        noticeCrawlPort.failingTopics += "EVENT_NEWS"
        noticeCrawlPort.details[FakeNoticeCrawlPort.contentUrl(101)] = NoticeDetail("본문 101", "https://img/101.png")
        noticeCrawlPort.details[FakeNoticeCrawlPort.contentUrl(200)] = NoticeDetail("본문 200", null)
        // 102 는 상세 페이지를 읽지 못한다
        noticeSummaryPort.failingContents += "본문 200"

        val ko = token("token-ko", "ko", 1, 2)
        val ja = token("token-ja", "ja-JP", 1)
        token("token-inactive", "ko", 1, active = false)
        token("token-invalid", "ko", 2)
        pushPort.invalidTokens += "token-invalid"

        val execution = run(noticeCrawlJob, mapOf("topicType" to "NOTICE"))

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)

        // 1. 크롤링 : 새 공지만 저장
        val notices = noticeRepository.findAll().associateBy { it.nttId }
        assertThat(notices.keys).containsExactlyInAnyOrder(99, 100, 101, 102, 200)
        assertThat(notices.getValue(101).contentImageUrl).isEqualTo("https://img/101.png")
        assertThat(contentOf(notices.getValue(102))).isNull()
        assertThat(webhookPort.messages.filter { it.type == SlackType.CRAWL_ERROR }).singleElement()
            .satisfies({ assertThat(it.content).contains("EVENT_NEWS") })

        // 2. 발송 : 일반소식은 남은 공지까지 3건이라 한 알림으로 묶는다. 비활성 토큰은 대상이 아니다
        val sentByTokens = pushPort.sent.associate { it.tokens to requireNotNull(it.message) }
        assertThat(sentByTokens.keys).containsExactlyInAnyOrder(listOf("token-ko"), listOf("token-ja"), listOf("token-ko", "token-invalid"))
        assertThat(sentByTokens.getValue(listOf("token-ko"))).satisfies({
            assertThat(it.title).isEqualTo("일반소식")
            assertThat(it.body).isEqualTo("공지 102 외 2개의 소식이 있습니다.")
        })
        assertThat(sentByTokens.getValue(listOf("token-ja"))).satisfies({
            assertThat(it.title).isEqualTo("一般ニュース")
            assertThat(it.body).isEqualTo("공지 102 ほか2件")
        })
        assertThat(sentByTokens.getValue(listOf("token-ko", "token-invalid"))).satisfies({
            assertThat(it.title).isEqualTo("장학안내")
            assertThat(it.body).isEqualTo("장학 200")
        })
        assertThat(fcmTokenRepository.findByToken("token-invalid")!!.isActive).isFalse()
        assertThat(fcmTokenRepository.findByToken(ko.token)!!.isActive).isTrue()
        assertThat(ja.isActive).isTrue()

        val sent = noticeRepository.findAll().associateBy { it.nttId }
        assertThat(listOf(100L, 101L, 102L, 200L).map { sent.getValue(it).notificationStatus }).containsOnly(NotificationStatus.SENT)
        assertThat(listOf(100L, 101L, 102L, 200L).map { sent.getValue(it).notifiedAt }).doesNotContainNull()
        // 이미 보낸 공지는 다시 표시하지 않는다
        assertThat(sent.getValue(99).notifiedAt).isEqualTo(LocalDate.of(2026, 9, 29).atStartOfDay())

        // 3. 요약 : 실패는 시도 횟수만 늘리고 다음 실행에서 다시 시도한다
        assertThat(sent.getValue(100).summaryStatus).isEqualTo(SummaryStatus.COMPLETED)
        assertThat(contentOf(sent.getValue(100))?.contentSummary).isEqualTo("요약: 본문 100")
        assertThat(sent.getValue(101).summaryStatus).isEqualTo(SummaryStatus.COMPLETED)
        assertThat(sent.getValue(102).summaryStatus).isEqualTo(SummaryStatus.SKIPPED)
        assertThat(sent.getValue(200).summaryStatus).isEqualTo(SummaryStatus.PENDING)
        assertThat(sent.getValue(200).summaryAttemptCount).isEqualTo(1)
    }

    @Test
    fun `새 공지가 없으면 아무것도 보내지 않는다`() {
        saveNotice(99, generalNews, NotificationStatus.SENT, content = null)
        noticeCrawlPort.lists["GENERAL_NEWS"] = listOf(Row(99, "공지 99"))
        token("token-ko", "ko", 1)

        val execution = run(noticeCrawlJob, mapOf("topicType" to "NOTICE"))

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(pushPort.sent).isEmpty()
    }

    @Test
    fun `학과 Job 은 학과 게시판 공지만 발송한다`() {
        saveNotice(100, generalNews, NotificationStatus.PENDING, content = null)
        token("token-ko", "ko", 1)

        val execution = run(noticeCrawlJob, mapOf("topicType" to "MAJOR"))

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(pushPort.sent).isEmpty()
        assertThat(noticeRepository.findByNttId(100)!!.notificationStatus).isEqualTo(NotificationStatus.PENDING)
    }

    private fun saveNotice(nttId: Long, topic: TopicView, status: NotificationStatus, content: String?) {
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
        if (status == NotificationStatus.SENT) {
            notice.markNotified(LocalDate.of(2026, 9, 29).atStartOfDay())
            notice.skipSummary()
        }
        val saved = noticeRepository.save(notice)
        noticeContentRepository.save(NoticeContent(requireNotNull(saved.id), content, null))
    }

    private fun contentOf(notice: Notice): NoticeContent? =
        noticeContentRepository.findByNoticeId(requireNotNull(notice.id))?.takeIf { it.content != null || it.contentSummary != null }

    private fun token(value: String, language: String, vararg topicCodes: Int, active: Boolean = true): FcmToken {
        val token = fcmTokenRepository.save(FcmToken(value, DeviceType.iOS, language).apply { if (!active) deactivate() })
        topicCodes.forEach { code ->
            fcmTokenSubscriptionRepository.save(FcmTokenSubscription(requireNotNull(token.id), code, if (code == 1) "GENERAL_NEWS" else "SCHOLARSHIP_NEWS"))
        }
        return token
    }

    private fun topic(code: Int, name: String) =
        TopicView(code.toLong(), code, name, TopicType.NOTICE, LocalizedText(name), null, "https://www.ut.ac.kr", "/", true, true)

}
