package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.api.config.querydsl.QuerydslConfig
import com.fx.api.domain.NoticeQuery
import com.fx.api.fixture.TopicFixture
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NotificationStatus
import com.fx.common.domain.notice.SummaryStatus
import com.fx.common.exception.NoticeException
import com.fx.persistence.MySqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.LocalDate

/** 실제 MySQL 에서 공지 저장 · keyset 목록 조회 · 삭제를 검증한다. */
@DataJpaTest(properties = ["DB_URL=unused", "DB_USERNAME=unused", "DB_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, QuerydslConfig::class, NoticeQueryRepository::class, NoticePersistenceAdapter::class)
class NoticePersistenceAdapterTest @Autowired constructor(
    private val noticePersistenceAdapter: NoticePersistenceAdapter,
    private val entityManager: EntityManager,
) {

    @Test
    fun `관리자 공지를 요약과 함께 저장하면 본문 행에 요약이 들어간다`() {
        val saved = noticePersistenceAdapter.create(adminNotice(nttId = 100, hasSummary = true), "요약")
        flushAndClear()

        val found = noticePersistenceAdapter.getNotice(100)
        assertThat(found.id).isEqualTo(saved.id)
        assertThat(found.topicCode).isEqualTo(1)
        assertThat(found.topicName).isEqualTo("GENERAL_NEWS")
        assertThat(found.notificationStatus).isEqualTo(NotificationStatus.SKIPPED)
        assertThat(found.summaryStatus).isEqualTo(SummaryStatus.COMPLETED)
        assertThat(noticePersistenceAdapter.findNoticeContent(requireNotNull(saved.id))?.contentSummary).isEqualTo("요약")
    }

    @Test
    fun `공지가 없으면 예외가 발생한다`() {
        assertThat(noticePersistenceAdapter.existsByNttId(100)).isFalse()
        assertThatThrownBy {
            noticePersistenceAdapter.getNotice(100)
        }.isInstanceOf(NoticeException::class.java)
    }

    @Test
    fun `목록은 nttId 내림차순으로 커서 이전의 공지를 size 개 읽는다`() {
        (1L..5L).forEach {
            noticePersistenceAdapter.create(crawledNotice(it), null)
        }
        flushAndClear()

        val firstPage = noticePersistenceAdapter.findNotices(NoticeQuery(size = 2))
        val nextPage = noticePersistenceAdapter.findNotices(NoticeQuery(nttId = firstPage.last().nttId, size = 2))

        assertThat(firstPage.map {
            it.nttId
        }).containsExactly(5L, 4L)
        assertThat(nextPage.map {
            it.nttId
        }).containsExactly(3L, 2L)
    }

    @Test
    fun `토픽 코드와 제목 키워드로 거른다`() {
        noticePersistenceAdapter.create(crawledNotice(1, title = "Scholarship 안내"), null)
        noticePersistenceAdapter.create(crawledNotice(2, title = "수강신청 안내"), null)
        noticePersistenceAdapter.create(crawledNotice(3, title = "scholarship 선발", topic = TopicFixture.COMPUTER_SOFTWARE), null)
        flushAndClear()

        assertThat(noticePersistenceAdapter.findNotices(NoticeQuery(topicCode = 1, size = 10)).map {
            it.nttId
        })
            .containsExactly(2L, 1L)
        // 대소문자를 구분하지 않는다 (utf8mb4_0900_ai_ci)
        assertThat(noticePersistenceAdapter.findNotices(NoticeQuery(keyword = "SCHOLARSHIP", size = 10)).map {
            it.nttId
        })
            .containsExactly(3L, 1L)
        assertThat(noticePersistenceAdapter.findNotices(NoticeQuery(topicCode = 300, keyword = "선발", size = 10)).map {
            it.nttId
        })
            .containsExactly(3L)
    }

    @Test
    fun `공지를 지우면 본문 행도 함께 지워진다`() {
        val saved = noticePersistenceAdapter.create(adminNotice(nttId = 100, hasSummary = true), "요약")
        flushAndClear()

        noticePersistenceAdapter.delete(noticePersistenceAdapter.getNotice(100))
        flushAndClear()

        assertThat(noticePersistenceAdapter.existsByNttId(100)).isFalse()
        assertThat(noticePersistenceAdapter.findNoticeContent(requireNotNull(saved.id))).isNull()
    }

    private fun crawledNotice(nttId: Long, title: String = "공지 $nttId", topic: TopicView = TopicFixture.GENERAL_NEWS) =
        Notice.crawled(
            nttId = nttId,
            topic = topic,
            title = title,
            department = "학사팀",
            contentUrl = "https://www.ut.ac.kr/notice/$nttId",
            contentImageUrl = null,
            registrationDate = LocalDate.of(2026, 9, 30),
            isAttachment = false,
        )

    private fun adminNotice(nttId: Long, hasSummary: Boolean) =
        Notice.registeredByAdmin(
            nttId = nttId,
            topic = TopicFixture.GENERAL_NEWS,
            title = "관리자 공지",
            department = "학사팀",
            contentUrl = "https://www.ut.ac.kr/notice/$nttId",
            contentImageUrl = null,
            registrationDate = LocalDate.of(2026, 9, 30),
            isAttachment = true,
            hasSummary = hasSummary,
        )

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

}
