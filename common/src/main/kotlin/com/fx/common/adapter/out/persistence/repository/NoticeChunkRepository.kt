package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.notice.NoticeChunk
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NoticeChunkRepository : JpaRepository<NoticeChunk, Long> {

    fun findAllByNoticeIdOrderByChunkIndex(noticeId: Long): List<NoticeChunk>

    /**
     * 공지들의 청크를 한 번에 지운다. 다시 임베딩한 청크를 넣기 전에 부른다.
     * 파생 삭제(`deleteAllBy…`)는 청크를 하나씩 읽어 지우므로 벡터까지 읽지 않도록 쿼리로 지운다.
     * 같은 트랜잭션에서 바꾼 공지 상태가 사라지지 않도록 영속성 컨텍스트는 비우지 않는다 (청크는 읽어 두지 않으므로 남는 것이 없다).
     */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM NoticeChunk c WHERE c.noticeId IN :noticeIds")
    fun deleteAllByNoticeIds(noticeIds: Collection<Long>): Int

}
