package com.fx.common.domain.notice

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/** 임베딩 벡터 차원 — `notice_chunk.embedding` 의 `vector(768)` 과 같아야 한다. */
const val EMBEDDING_DIMENSIONS = 768

/** 임베딩 모델 표기 최대 길이 — `embedding_model` 컬럼 길이와 같아야 한다. */
const val EMBEDDING_MODEL_MAX_LENGTH = 50

/**
 * RAG 검색 단위인 공지 청크. 공지 하나를 제목 · 본문 조각 · 이미지 단위로 나눠 벡터와 함께 저장한다.
 *
 * - 임베딩 Job 이 공지의 청크를 통째로 지우고 다시 넣으므로 한 번 저장한 청크는 바꾸지 않는다.
 * - 공지는 하드 삭제되므로 외래 키를 두지 않는다. 공지가 없어진 청크는 정리 Job 이 지운다 (이번 범위 밖).
 * - [embedding] 은 hibernate-vector 가 pgvector `vector` 와 `FloatArray` 를 서로 바꾼다.
 *   벡터 검색(`<=>`)은 질문 API 에서 네이티브 쿼리로 한다.
 */
@Entity
@Table(
    name = "notice_chunk",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_notice_chunk_notice_id_chunk_index", columnNames = ["notice_id", "chunk_index"]),
    ],
)
class NoticeChunk(
    noticeId: Long,
    chunkIndex: Int,
    chunkType: NoticeChunkType,
    content: String,
    imageUrl: String?,
    embedding: FloatArray,
    embeddingModel: String,
) : BaseEntity() {

    init {
        require(embedding.size == EMBEDDING_DIMENSIONS) {
            "임베딩은 $EMBEDDING_DIMENSIONS 차원이어야 합니다: ${embedding.size}"
        }
    }

    @Column(name = "notice_id", nullable = false, updatable = false, comment = "notice.id")
    val noticeId: Long = noticeId

    @Column(name = "chunk_index", nullable = false, updatable = false, comment = "공지 안에서의 순서 (0부터)")
    val chunkIndex: Int = chunkIndex

    @Enumerated(EnumType.STRING)
    @Column(name = "chunk_type", nullable = false, updatable = false, length = 20, comment = "TITLE / BODY / IMAGE")
    val chunkType: NoticeChunkType = chunkType

    @Column(
        name = "content", nullable = false, updatable = false, columnDefinition = "TEXT",
        comment = "임베딩한 텍스트 (BODY 는 제목 + 본문 조각, IMAGE 는 제목)",
    )
    val content: String = content

    @Column(
        name = "image_url", nullable = true, updatable = false, length = NOTICE_URL_MAX_LENGTH,
        comment = "IMAGE 청크의 원본 이미지 URL",
    )
    val imageUrl: String? = imageUrl

    @JdbcTypeCode(SqlTypes.VECTOR)
    @org.hibernate.annotations.Array(length = EMBEDDING_DIMENSIONS)
    @Column(name = "embedding", nullable = false, updatable = false, comment = "임베딩 벡터 (768차원)")
    val embedding: FloatArray = embedding

    @Column(
        name = "embedding_model", nullable = false, updatable = false, length = EMBEDDING_MODEL_MAX_LENGTH,
        comment = "임베딩 모델 · 차원 (예: gemini-embedding-2@768). 모델을 바꾸면 다른 값끼리는 비교하지 않는다",
    )
    val embeddingModel: String = embeddingModel

}
