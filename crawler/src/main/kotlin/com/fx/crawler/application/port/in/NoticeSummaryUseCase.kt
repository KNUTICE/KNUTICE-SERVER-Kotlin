package com.fx.crawler.application.port.`in`

import com.fx.common.domain.TopicType
import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget

interface NoticeSummaryUseCase {

    fun findTargets(topicType: TopicType, afterNoticeId: Long?, size: Int): List<SummaryTarget>

    /** 공지 하나를 요약한다. 실패해도 예외를 던지지 않고 결과로 돌려준다. */
    fun summarize(target: SummaryTarget): SummaryResult

    fun applyResults(results: List<SummaryResult>)

}
