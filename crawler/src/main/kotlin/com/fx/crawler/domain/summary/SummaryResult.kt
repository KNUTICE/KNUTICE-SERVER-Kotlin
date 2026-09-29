package com.fx.crawler.domain.summary

/** 공지 하나의 AI 요약 결과. */
sealed interface SummaryResult {

    val noticeId: Long

    data class Completed(override val noticeId: Long, val summary: String) : SummaryResult

    /** 요약 호출이 실패했다. 시도 횟수가 한도에 닿기 전까지 다음 실행에서 다시 시도한다. */
    data class Failed(override val noticeId: Long, val reason: String) : SummaryResult

    /** 요약할 본문이 없다. */
    data class Skipped(override val noticeId: Long) : SummaryResult

}
