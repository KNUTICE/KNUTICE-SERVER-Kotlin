package com.fx.common.domain.notice

/** 공지 AI 요약 상태. 목록 응답의 `isContentSummary` 는 [COMPLETED] 인지로 판단한다. */
enum class SummaryStatus {

    /** 요약 대기. 다음 요약 배치에서 시도한다. */
    PENDING,

    /** 요약 있음. */
    COMPLETED,

    /** 시도 한도까지 실패. 더 시도하지 않는다. */
    FAILED,

    /** 요약 대상 아님 (본문이 없는 관리자 등록 공지, 이관 전 요약이 없던 공지). */
    SKIPPED,

}
