package com.fx.common.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.NoticeChunkRepository
import com.fx.common.adapter.out.persistence.repository.NoticeRepository
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import com.fx.common.domain.notice.EmbeddingStatus
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeChunk
import com.fx.common.domain.notice.NoticeChunkType
import com.fx.persistence.PostgreSqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.LocalDate

/**
 * 임베딩 청크 · 공지 임베딩 상태 매핑을 실제 PostgreSQL(pgvector) 에서 검증한다.
 * `ddl-auto=validate` 이므로 엔티티 매핑과 V14 스키마가 어긋나면 컨텍스트가 뜨지 않는다.
 */
@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=validate"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgreSqlContainerConfig::class)
class NoticeChunkRepositoryTest @Autowired constructor(
    private val noticeChunkRepository: NoticeChunkRepository,
    private val noticeRepository: NoticeRepository,
    private val entityManager: EntityManager,
) {

    private fun vector(seed: Int) =
        FloatArray(EMBEDDING_DIMENSIONS) {
            ((it + seed) % 7 - 3) / 10f
        }

    private fun chunk(noticeId: Long, index: Int, type: NoticeChunkType = NoticeChunkType.BODY, imageUrl: String? = null) =
        NoticeChunk(noticeId, index, type, "[일반소식] 공지 $noticeId 청크 $index", imageUrl, vector(index), "gemini-embedding-2@768")

    @Test
    fun `청크를 벡터와 함께 저장하고 순서대로 다시 읽는다`() {
        noticeChunkRepository.saveAll(
            listOf(
                chunk(1L, 1, NoticeChunkType.IMAGE, "https://www.ut.ac.kr/upload/a.png"),
                chunk(1L, 0),
            )
        )
        entityManager.flush()
        entityManager.clear()

        val chunks = noticeChunkRepository.findAllByNoticeIdOrderByChunkIndex(1L)

        assertThat(chunks.map { it.chunkIndex }).containsExactly(0, 1)
        assertThat(chunks).allSatisfy {
            assertThat(it.id).isNotNull()
            assertThat(it.createdAt).isNotNull()
            assertThat(it.embedding).hasSize(EMBEDDING_DIMENSIONS)
            assertThat(it.embedding).containsExactly(*vector(it.chunkIndex))
        }
        assertThat(chunks[1].chunkType).isEqualTo(NoticeChunkType.IMAGE)
        assertThat(chunks[1].imageUrl).isEqualTo("https://www.ut.ac.kr/upload/a.png")
    }

    @Test
    fun `지정한 공지의 청크만 지운다`() {
        noticeChunkRepository.saveAll(listOf(chunk(1L, 0), chunk(1L, 1), chunk(2L, 0)))
        entityManager.flush()

        val deleted = noticeChunkRepository.deleteAllByNoticeIds(listOf(1L))

        assertThat(deleted).isEqualTo(2)
        assertThat(noticeChunkRepository.findAll().map { it.noticeId }).containsExactly(2L)
    }

    @Test
    fun `768 차원이 아닌 벡터로는 청크를 만들 수 없다`() {
        assertThatThrownBy {
            NoticeChunk(1L, 0, NoticeChunkType.TITLE, "제목", null, FloatArray(3), "gemini-embedding-2@768")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `새 공지는 임베딩 대기 상태로 저장된다`() {
        val topic = TopicView(1L, 1, "GENERAL_NEWS", TopicType.NOTICE, LocalizedText("일반소식"), null, "https://www.ut.ac.kr", "/", true, true, true)
        val saved = noticeRepository.save(
            Notice.crawled(1001, topic, "공지", "학사팀", "https://www.ut.ac.kr/notice?nttId=1001", null, LocalDate.of(2026, 11, 16), false)
        )
        entityManager.flush()
        entityManager.clear()

        val notice = noticeRepository.findById(requireNotNull(saved.id)).orElseThrow()

        assertThat(notice.embeddingStatus).isEqualTo(EmbeddingStatus.PENDING)
        assertThat(notice.embeddingAttemptCount).isZero()
    }

}
