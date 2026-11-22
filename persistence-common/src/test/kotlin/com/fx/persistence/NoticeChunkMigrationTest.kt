package com.fx.persistence

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.simple.JdbcClient

/** V14 의 RAG 테이블(notice_chunk · notice.embedding_status)이 pgvector 와 함께 동작하는지 확인한다. */
@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgreSqlContainerConfig::class)
class NoticeChunkMigrationTest @Autowired constructor(
    private val jdbcClient: JdbcClient,
) {

    @Test
    fun `notice_chunk 의 embedding 은 768 차원 vector 다`() {
        val type = jdbcClient
            .sql("SELECT format_type(atttypid, atttypmod) FROM pg_attribute WHERE attrelid = 'notice_chunk'::regclass AND attname = 'embedding'")
            .query(String::class.java)
            .single()

        assertThat(type).isEqualTo("vector(768)")
    }

    @Test
    fun `기존 공지는 임베딩 대기 상태로 시작한다`() {
        insertNotice(id = 1L)

        val status = jdbcClient
            .sql("SELECT embedding_status FROM notice WHERE id = 1")
            .query(String::class.java)
            .single()
        val attemptCount = jdbcClient
            .sql("SELECT embedding_attempt_count FROM notice WHERE id = 1")
            .query(Int::class.java)
            .single()

        assertThat(status).isEqualTo("PENDING")
        assertThat(attemptCount).isZero()
    }

    @Test
    fun `질문 벡터와 코사인 거리가 가까운 청크부터 찾는다`() {
        insertChunk(id = 1L, chunkIndex = 0, embedding = unitVector(0))
        insertChunk(id = 2L, chunkIndex = 1, embedding = unitVector(1))

        // <=> 는 pgvector 의 코사인 거리 연산자다 (0 에 가까울수록 비슷하다)
        val nearest = jdbcClient
            .sql("SELECT id FROM notice_chunk ORDER BY embedding <=> CAST(:query AS vector) LIMIT 1")
            .param("query", unitVector(1))
            .query(Long::class.java)
            .single()

        assertThat(nearest).isEqualTo(2L)
    }

    @Test
    fun `768 차원이 아닌 벡터는 저장되지 않는다`() {
        assertThatThrownBy { insertChunk(id = 1L, chunkIndex = 0, embedding = "[1,0,0]") }
            .isInstanceOf(DataAccessException::class.java)
    }

    @Test
    fun `한 공지 안에서 같은 순서의 청크는 하나뿐이다`() {
        insertChunk(id = 1L, chunkIndex = 0, embedding = unitVector(0))

        assertThatThrownBy { insertChunk(id = 2L, chunkIndex = 0, embedding = unitVector(1)) }
            .isInstanceOf(DataAccessException::class.java)
    }

    private fun insertNotice(id: Long) {
        // 임베딩 컬럼은 넣지 않는다. 기존 코드(Notice 엔티티)가 저장할 때처럼 DB 기본값이 채워지는지 본다
        jdbcClient
            .sql(
                """
                INSERT INTO notice (id, ntt_id, topic_code, topic_name, title, department, content_url, content_image_url,
                                    registration_date, is_attachment, notification_status, summary_status, created_at, updated_at)
                VALUES (:id, :id, 1, '일반공지', '장학금 신청 안내', '학생지원과', 'https://www.ut.ac.kr/', NULL,
                        CURRENT_DATE, FALSE, 'SENT', 'COMPLETED', now(), now())
                """
            )
            .param("id", id)
            .update()
    }

    private fun insertChunk(id: Long, chunkIndex: Int, embedding: String) {
        jdbcClient
            .sql(
                """
                INSERT INTO notice_chunk (id, notice_id, chunk_index, chunk_type, content, image_url, embedding, embedding_model, created_at, updated_at)
                VALUES (:id, 100, :chunkIndex, 'BODY', '장학금 신청 안내 · 신청 기간은 9월 12일까지', NULL, CAST(:embedding AS vector), 'gemini-embedding-2@768', now(), now())
                """
            )
            .param("id", id)
            .param("chunkIndex", chunkIndex)
            .param("embedding", embedding)
            .update()
    }

    /** index 번째 성분만 1 인 768 차원 벡터 (pgvector 문자열 형식). */
    private fun unitVector(index: Int): String =
        (0 until DIMENSIONS).joinToString(",", "[", "]") { if (it == index) "1" else "0" }

    companion object {
        private const val DIMENSIONS = 768
    }

}
