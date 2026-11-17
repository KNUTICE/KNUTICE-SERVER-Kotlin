package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.NoticeChunkRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.notice.NoticeChunk
import com.fx.crawler.adapter.out.persistence.repository.NoticeEmbeddingQueryRepository
import com.fx.crawler.application.port.out.NoticeEmbeddingPersistencePort
import com.fx.crawler.domain.embedding.EmbeddingTarget
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class NoticeEmbeddingPersistenceAdapter(
    private val noticeChunkRepository: NoticeChunkRepository,
    private val noticeEmbeddingQueryRepository: NoticeEmbeddingQueryRepository,
) : NoticeEmbeddingPersistencePort {

    @Transactional(readOnly = true)
    override fun findEmbeddingTargets(retryBefore: LocalDateTime, beforeNttId: Long?, size: Int): List<EmbeddingTarget> =
        noticeEmbeddingQueryRepository.findEmbeddingTargets(retryBefore, beforeNttId, size)

    /** 지우기 · 넣기를 한 트랜잭션으로 묶어, 검색하는 쪽이 청크가 빈 공지를 보지 않게 한다. */
    @Transactional
    override fun replaceChunks(chunksByNoticeId: Map<Long, List<NoticeChunk>>) {
        if (chunksByNoticeId.isEmpty()) {
            return
        }
        noticeChunkRepository.deleteAllByNoticeIds(chunksByNoticeId.keys)
        noticeChunkRepository.saveAll(chunksByNoticeId.values.flatten())
    }

}
