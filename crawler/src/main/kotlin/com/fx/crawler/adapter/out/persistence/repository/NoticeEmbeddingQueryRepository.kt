package com.fx.crawler.adapter.out.persistence.repository

import com.fx.common.domain.notice.EmbeddingStatus
import com.fx.common.domain.notice.QNotice.Companion.notice
import com.fx.common.domain.notice.QNoticeContent.Companion.noticeContent
import com.fx.crawler.domain.embedding.EmbeddingTarget
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class NoticeEmbeddingQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    /**
     * 임베딩 대기 공지와 본문. `idx_notice_embedding_status` 로 대기 공지만 읽고, 최신 게시글부터 ntt_id 내림차순 keyset 으로 넘긴다.
     * 처음 채울 때 수천 건이 대기이므로 질문이 많은 최근 공지부터 검색되게 한다.
     * 요약과 같이, 실패한 공지는 시도 횟수가 늘면서 바뀐 `updated_at` 을 마지막 시도 시각으로 본다.
     */
    fun findEmbeddingTargets(retryBefore: LocalDateTime, beforeNttId: Long?, size: Int): List<EmbeddingTarget> =
        queryFactory
            .select(notice.id, notice.nttId, notice.topicCode, notice.topicName, notice.title, notice.contentUrl, noticeContent.content)
            .from(notice)
            .leftJoin(noticeContent).on(noticeContent.noticeId.eq(notice.id))
            .where(
                notice.embeddingStatus.eq(EmbeddingStatus.PENDING),
                notice.embeddingAttemptCount.eq(0).or(notice.updatedAt.lt(retryBefore)),
                beforeNttId?.let {
                    notice.nttId.lt(it)
                },
            )
            .orderBy(notice.nttId.desc())
            .limit(size.toLong())
            .fetch()
            .map {
                EmbeddingTarget(
                    noticeId = requireNotNull(it.get(notice.id)),
                    nttId = requireNotNull(it.get(notice.nttId)),
                    topicCode = requireNotNull(it.get(notice.topicCode)),
                    topicName = requireNotNull(it.get(notice.topicName)),
                    title = requireNotNull(it.get(notice.title)),
                    contentUrl = requireNotNull(it.get(notice.contentUrl)),
                    content = it.get(noticeContent.content),
                )
            }

}
