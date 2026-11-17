package com.fx.crawler.application.port.out

import com.fx.common.domain.notice.NoticeChunk
import com.fx.crawler.domain.embedding.EmbeddingTarget
import java.time.LocalDateTime

interface NoticeEmbeddingPersistencePort {

    /**
     * 임베딩 대기 공지를 최신 게시글(게시글 번호가 큰 것)부터 [beforeNttId] 다음 [size] 개.
     * 임베딩에 실패한 적이 있는 공지는 마지막 시도가 [retryBefore] 보다 전일 때만 포함한다.
     */
    fun findEmbeddingTargets(retryBefore: LocalDateTime, beforeNttId: Long?, size: Int): List<EmbeddingTarget>

    /** 공지마다 옛 청크를 모두 지우고 새 청크로 바꾼다. 호출하는 쪽의 트랜잭션 안에서 부른다. */
    fun replaceChunks(chunksByNoticeId: Map<Long, List<NoticeChunk>>)

}
