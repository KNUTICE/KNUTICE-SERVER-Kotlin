package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeChunkSearchRepository
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.api.fixture.TopicFixture
import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeChunk
import com.fx.common.domain.notice.NoticeChunkType
import com.fx.persistence.PostgreSqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.sqrt

/** 실제 PostgreSQL + pgvector 에서 청크 검색 점수 · 정렬과 질문 기록 저장 · 세기를 검증한다. */
@DataJpaTest(properties = ["POSTGRES_URL=unused", "POSTGRES_DATABASE=unused", "POSTGRES_USERNAME=unused", "POSTGRES_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgreSqlContainerConfig::class, NoticeChunkSearchRepository::class, NoticeQuestionPersistenceAdapter::class)
class NoticeQuestionPersistenceAdapterTest @Autowired constructor(
    private val adapter: NoticeQuestionPersistenceAdapter,
    private val entityManager: EntityManager,
) {

    private val today = LocalDate.of(2026, 11, 19)
    private val model = "gemini-embedding-2@768"

    @Test
    fun `유사도가 같으면 최근 가산으로 올해 공지가 지난해 공지보다 위에 온다`() {
        val old = notice(1, LocalDate.of(2024, 11, 15))
        val recent = notice(2, LocalDate.of(2026, 11, 10))
        chunk(old, 0, NoticeChunkType.TITLE, axis(0))
        chunk(recent, 0, NoticeChunkType.BODY, axis(0))
        flushAndClear()

        val plain = adapter.searchChunks(axis(0), model, NoticeSearchRanking(0.0, 0.0), today, 10)
        val decayed = adapter.searchChunks(axis(0), model, NoticeSearchRanking(0.04, 0.0), today, 10)

        assertThat(plain.map {
            it.similarity
        }).allSatisfy {
            assertThat(it).isCloseTo(1.0, within(1e-6))
        }
        assertThat(decayed.map {
            it.nttId
        }).containsExactly(2L, 1L)
        // 2024-11-15 → 2026-11-19 는 734일
        assertThat(decayed[1].score).isCloseTo(1.0 - 0.04 * 734 / 365.0, within(1e-6))
        assertThat(decayed[0].title).isEqualTo("공지 2")
        assertThat(decayed[0].registrationDate).isEqualTo(LocalDate.of(2026, 11, 10))
        assertThat(decayed[0].contentUrl).isEqualTo("https://www.ut.ac.kr/2")
    }

    @Test
    fun `점수는 코사인 유사도 순이고, 제목만 청크 감점과 모델 표기를 따른다`() {
        val a = notice(1, today)
        val b = notice(2, today)
        chunk(a, 0, NoticeChunkType.TITLE, axis(0))
        chunk(b, 0, NoticeChunkType.BODY, mix(0, 1)) // 유사도 0.707
        chunk(b, 1, NoticeChunkType.BODY, axis(0), embeddingModel = "other-model@768")
        flushAndClear()

        val noPenalty = adapter.searchChunks(axis(0), model, NoticeSearchRanking(0.0, 0.0), today, 10)
        val titlePenalty = adapter.searchChunks(axis(0), model, NoticeSearchRanking(0.0, 0.4), today, 10)

        assertThat(noPenalty.map {
            it.chunkType
        }).containsExactly(NoticeChunkType.TITLE, NoticeChunkType.BODY)
        assertThat(noPenalty[1].similarity).isCloseTo(1 / sqrt(2.0), within(1e-6))
        assertThat(titlePenalty.map {
            it.chunkType
        }).containsExactly(NoticeChunkType.BODY, NoticeChunkType.TITLE)
        assertThat(adapter.searchChunks(axis(0), model, NoticeSearchRanking(0.0, 0.0), today, 1)).hasSize(1)
    }

    @Test
    fun `질문 기록을 저장하고 구간 시작 이후 질문 수를 센다`() {
        val saved = adapter.save(question())
        adapter.save(question())
        flushAndClear()

        assertThat(adapter.countSince(LocalDateTime.now().minusMinutes(1))).isEqualTo(2)
        assertThat(adapter.countSince(LocalDateTime.now().plusMinutes(1))).isZero()

        val found = adapter.getQuestion(requireNotNull(saved.id))
        found.giveFeedback(NoticeQuestionVerdict.BAD, "작년 날짜")
        flushAndClear()

        val reloaded = adapter.getQuestion(requireNotNull(saved.id))
        assertThat(reloaded.verdict).isEqualTo(NoticeQuestionVerdict.BAD)
        assertThat(reloaded.memo).isEqualTo("작년 날짜")
        assertThat(reloaded.sourceNttIds).containsExactly(11L, 12L)
    }

    @Test
    fun `없는 기록이면 예외가 발생한다`() {
        assertThatThrownBy {
            adapter.getQuestion(1)
        }.isInstanceOf(QuestionException::class.java)
    }

    private fun question() =
        NoticeQuestion(
            askedBy = NoticeQuestionAsker.ADMIN,
            askerKey = "10",
            question = "동계 계절학기 수강신청 언제야?",
            status = NoticeQuestionStatus.ANSWERED,
            answer = "11월 17일부터예요. [1]",
            sourceNttIds = listOf(11, 12),
            topSimilarity = 0.745,
            decayPerYear = 0.04,
            titlePenalty = 0.0,
            answerModel = "gemini-3.5-flash-lite",
            inputTokens = 3120,
            outputTokens = 96,
            elapsedMs = 2400,
        )

    private fun notice(nttId: Long, registrationDate: LocalDate): Notice =
        Notice.crawled(
            nttId = nttId,
            topic = TopicFixture.GENERAL_NEWS,
            title = "공지 $nttId",
            department = "학사팀",
            contentUrl = "https://www.ut.ac.kr/$nttId",
            contentImageUrl = null,
            registrationDate = registrationDate,
            isAttachment = false,
        ).also(entityManager::persist)

    private fun chunk(
        notice: Notice,
        index: Int,
        type: NoticeChunkType,
        embedding: FloatArray,
        embeddingModel: String = model,
    ) = entityManager.persist(
        NoticeChunk(
            noticeId = requireNotNull(notice.id),
            chunkIndex = index,
            chunkType = type,
            content = "[일반소식] ${notice.title}\n본문 $index",
            imageUrl = null,
            embedding = embedding,
            embeddingModel = embeddingModel,
        )
    )

    private fun axis(i: Int) =
        FloatArray(EMBEDDING_DIMENSIONS) {
            if (it == i) 1f else 0f
        }

    private fun mix(i: Int, j: Int): FloatArray {
        val v = (1 / sqrt(2.0)).toFloat()
        return FloatArray(EMBEDDING_DIMENSIONS) {
            if (it == i || it == j) v else 0f
        }
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

}
