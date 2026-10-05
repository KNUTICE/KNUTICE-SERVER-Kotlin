package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.NoticeQuery
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.QNotice.Companion.notice
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

/**
 * 공지 목록 조회. `ntt_id` 를 커서로 쓰는 keyset 방식이며 정렬은 `ntt_id` 내림차순 하나만 쓴다.
 * 토픽 필터가 있으면 `idx_notice_topic_code_ntt_id`, 없으면 `uk_notice_ntt_id` 를 탄다.
 */
@Repository
class NoticeQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun findNotices(noticeQuery: NoticeQuery): List<Notice> =
        queryFactory
            .selectFrom(notice)
            .where(
                topicCodeEq(noticeQuery.topicCode),
                titleContains(noticeQuery.keyword),
                nttIdLt(noticeQuery.nttId),
            )
            .orderBy(notice.nttId.desc())
            .limit(noticeQuery.size.toLong())
            .fetch()

    private fun topicCodeEq(topicCode: Int?): BooleanExpression? =
        topicCode?.let {
            notice.topicCode.eq(it)
        }

    private fun titleContains(keyword: String?): BooleanExpression? =
        keyword?.let {
            notice.title.contains(it)
        }

    private fun nttIdLt(nttId: Long?): BooleanExpression? =
        nttId?.let {
            notice.nttId.lt(it)
        }

}
