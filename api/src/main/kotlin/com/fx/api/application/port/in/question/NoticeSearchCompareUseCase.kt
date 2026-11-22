package com.fx.api.application.port.`in`.question

import com.fx.api.application.port.`in`.question.dto.CompareSearchCommand
import com.fx.api.domain.question.NoticeSearchComparison
import com.fx.api.domain.question.QuestionQuota

/**
 * 관리자 검색 비교 (답변 없이 검색만). 같은 질문을 "유사도만"(A)과 "검색 설정 적용"(B)으로 나란히 돌려준다.
 *
 * - 질문 기록을 남기지 않고, 질문 한도(앱 · 관리자 · 서버 전체)에도 세지 않는다.
 * - 같은 질문의 임베딩은 서버 메모리에 두고 다시 써서, 검색 설정만 바꿔 다시 비교할 때는 서버 키를 쓰지 않는다.
 */
interface NoticeSearchCompareUseCase {

    /**
     * @throws com.fx.api.exception.QuestionException 하루 새 질문 수 (COMPARE_DAILY_LIMIT_EXCEEDED), 임베딩 한도 · 장애 (AI_RATE_LIMITED · AI_UNAVAILABLE)
     */
    fun compare(command: CompareSearchCommand): NoticeSearchComparison

    /** 오늘 검색 비교가 새로 임베딩한 질문 수 */
    fun getUsage(): QuestionQuota

}
