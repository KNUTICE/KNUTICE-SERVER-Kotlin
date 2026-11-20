package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.common.domain.notice.NoticeChunkType
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * pgvector 코사인 거리(`<=>`)로 공지 청크를 찾는 네이티브 쿼리.
 *
 * - 점수 = 유사도 − decayPerYear × 지난 해 수 − (제목만 청크면 titlePenalty). 점수 순으로 [search] 의 limit 개를 읽는다.
 * - 벡터 인덱스 없이 전체를 비교한다 (청크 수천 개). 청크가 크게 늘면 HNSW 인덱스와 함께 쿼리를 다시 본다.
 * - 같은 임베딩 모델 · 차원으로 만든 청크끼리만 비교한다 (`embedding_model`).
 */
@Repository
class NoticeChunkSearchRepository(
    private val entityManager: EntityManager,
) {

    fun search(
        embedding: FloatArray,
        embeddingModel: String,
        ranking: NoticeSearchRanking,
        today: LocalDate,
        limit: Int,
    ): List<NoticeChunkHit> {
        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(SEARCH_SQL)
            .setParameter("embedding", embedding.joinToString(",", "[", "]"))
            .setParameter("embeddingModel", embeddingModel)
            .setParameter("decayPerYear", ranking.decayPerYear)
            .setParameter("titlePenalty", ranking.titlePenalty)
            .setParameter("today", today)
            .setParameter("limit", limit)
            .resultList as List<Array<Any?>>

        return rows.map { row ->
            NoticeChunkHit(
                noticeId = (row[0] as Number).toLong(),
                nttId = (row[1] as Number).toLong(),
                title = row[2] as String,
                registrationDate = toLocalDate(row[3]),
                contentUrl = row[4] as String,
                chunkIndex = (row[5] as Number).toInt(),
                chunkType = NoticeChunkType.valueOf(row[6] as String),
                content = row[7] as String,
                similarity = (row[8] as Number).toDouble(),
                score = (row[9] as Number).toDouble(),
            )
        }
    }

    private fun toLocalDate(value: Any?): LocalDate =
        when (value) {
            is LocalDate -> value
            is java.sql.Date -> value.toLocalDate()
            else -> error("등록일을 읽지 못했습니다: $value")
        }

    companion object {
        private val SEARCH_SQL = """
            SELECT c.notice_id,
                   n.ntt_id,
                   n.title,
                   n.registration_date,
                   n.content_url,
                   c.chunk_index,
                   c.chunk_type,
                   c.content,
                   1 - (c.embedding <=> CAST(:embedding AS vector)) AS similarity,
                   1 - (c.embedding <=> CAST(:embedding AS vector))
                       - :decayPerYear * GREATEST(0, CAST(:today AS date) - n.registration_date) / 365.0
                       - CASE WHEN c.chunk_type = 'TITLE' THEN :titlePenalty ELSE 0 END AS score
            FROM notice_chunk c
            JOIN notice n ON n.id = c.notice_id
            WHERE c.embedding_model = :embeddingModel
            ORDER BY score DESC, c.notice_id, c.chunk_index
            LIMIT :limit
        """.trimIndent()
    }

}
