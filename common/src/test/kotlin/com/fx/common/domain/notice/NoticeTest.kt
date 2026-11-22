package com.fx.common.domain.notice

import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.LocalizedText
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class NoticeTest {

    private fun topic(summaryEnabled: Boolean) =
        TopicView(1L, 1, "GENERAL_NEWS", TopicType.NOTICE, LocalizedText("일반소식"), null, "https://www.ut.ac.kr", "/", true, true, summaryEnabled)

    private fun crawled(topic: TopicView) =
        Notice.crawled(1, topic, "공지", "학사팀", "https://www.ut.ac.kr/notice?nttId=1", null, LocalDate.of(2026, 10, 5), false)

    @Test
    fun `요약이 켜진 토픽의 새 공지는 요약 대기로 저장한다`() {
        val notice = crawled(topic(summaryEnabled = true))

        assertThat(notice.notificationStatus).isEqualTo(NotificationStatus.PENDING)
        assertThat(notice.summaryStatus).isEqualTo(SummaryStatus.PENDING)
    }

    @Test
    fun `새 공지는 임베딩 대기로 시작하고, 시도 한도까지 실패하면 더 시도하지 않는다`() {
        val notice = crawled(topic(summaryEnabled = true))
        assertThat(notice.embeddingStatus).isEqualTo(EmbeddingStatus.PENDING)

        notice.failEmbedding(maxAttempts = 2)
        assertThat(notice.embeddingStatus).isEqualTo(EmbeddingStatus.PENDING)
        assertThat(notice.embeddingAttemptCount).isEqualTo(1)

        notice.failEmbedding(maxAttempts = 2)
        assertThat(notice.embeddingStatus).isEqualTo(EmbeddingStatus.FAILED)
        assertThat(notice.embeddingAttemptCount).isEqualTo(2)
    }

    @Test
    fun `임베딩을 마치면 완료로 바꾼다`() {
        val notice = crawled(topic(summaryEnabled = false))

        notice.completeEmbedding()

        assertThat(notice.embeddingStatus).isEqualTo(EmbeddingStatus.COMPLETED)
    }

    @Test
    fun `요약이 꺼진 토픽의 새 공지는 요약하지 않음으로 확정한다`() {
        val notice = crawled(topic(summaryEnabled = false))

        assertThat(notice.notificationStatus).isEqualTo(NotificationStatus.PENDING)
        assertThat(notice.summaryStatus).isEqualTo(SummaryStatus.SKIPPED)
    }

}
