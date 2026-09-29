package com.fx.crawler.adapter.out.persistence.repository

import com.fx.common.domain.notice.QNotice.Companion.notice
import com.fx.common.domain.notice.QNoticeContent.Companion.noticeContent
import com.fx.common.domain.notice.SummaryStatus
import com.fx.crawler.domain.summary.SummaryTarget
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class NoticeSummaryQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    /** 요약 대기 공지와 본문. `idx_notice_summary_status` 로 대기 공지만 읽고 id keyset 으로 넘긴다. */
    fun findSummaryTargets(topicCodes: Collection<Int>, afterNoticeId: Long?, size: Int): List<SummaryTarget> =
        queryFactory
            .select(notice.id, notice.nttId, notice.topicCode, notice.title, noticeContent.content)
            .from(notice)
            .leftJoin(noticeContent).on(noticeContent.noticeId.eq(notice.id))
            .where(
                notice.summaryStatus.eq(SummaryStatus.PENDING),
                notice.topicCode.`in`(topicCodes),
                afterNoticeId?.let { notice.id.gt(it) },
            )
            .orderBy(notice.id.asc())
            .limit(size.toLong())
            .fetch()
            .map {
                SummaryTarget(
                    noticeId = requireNotNull(it.get(notice.id)),
                    nttId = requireNotNull(it.get(notice.nttId)),
                    topicCode = requireNotNull(it.get(notice.topicCode)),
                    title = requireNotNull(it.get(notice.title)),
                    content = it.get(noticeContent.content),
                )
            }

}
