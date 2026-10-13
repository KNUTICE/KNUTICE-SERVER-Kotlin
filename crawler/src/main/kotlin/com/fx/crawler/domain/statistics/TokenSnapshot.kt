package com.fx.crawler.domain.statistics

/**
 * 집계하는 시점의 기기별 토큰 수.
 * 비활성화 시각을 따로 남기지 않아 지난 날짜로는 다시 구할 수 없으므로, 그날 마감 직후(어제 날짜)에만 기록한다.
 * 기기를 알 수 없는 토큰(UNKNOWN)은 세지 않는다.
 */
data class TokenSnapshot(
    val activeIosTokenCount: Int,
    val activeAosTokenCount: Int,
    val inactiveIosTokenCount: Int,
    val inactiveAosTokenCount: Int,
)
