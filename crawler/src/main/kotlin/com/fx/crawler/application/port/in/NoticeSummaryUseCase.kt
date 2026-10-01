package com.fx.crawler.application.port.`in`

import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget

interface NoticeSummaryUseCase {

    /** 공지 · 학과 게시판의 요약 대기 공지. 실패한 적이 있는 공지는 재시도 간격이 지난 것만 포함한다. */
    fun findTargets(afterNoticeId: Long?, size: Int): List<SummaryTarget>

    /** 공지 하나를 요약한다. 실패해도 예외를 던지지 않고 결과로 돌려준다. */
    fun summarize(target: SummaryTarget): SummaryResult

    fun applyResults(results: List<SummaryResult>)

}
