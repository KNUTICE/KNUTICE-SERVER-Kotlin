package com.fx.crawler.application.port.out

import com.fx.crawler.domain.summary.SummaryRateLimitedException

/** 공지 본문 AI 요약. */
interface NoticeSummaryPort {

    /**
     * 본문을 마크다운으로 요약한다. 일시적인 오류는 몇 번 다시 시도한다.
     * @throws SummaryRateLimitedException 호출 한도에 걸렸을 때
     * @throws RuntimeException 끝내 요약하지 못했을 때
     */
    fun summarize(content: String): String

}
