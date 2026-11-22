package com.fx.api.domain.question

/**
 * 관리자 검색 비교 결과. 같은 질문 임베딩으로 두 번 검색해 공지 단위로 나란히 보여 준다.
 *
 * @property baseline A: 유사도만 (감점 없음)
 * @property ranked B: [ranking] 을 적용한 점수 순
 * @property embeddingCached 같은 질문을 이미 임베딩해 둬서 이번에 서버 키를 쓰지 않았으면 true
 * @property usage 오늘 검색 비교가 새로 임베딩한 질문 수
 */
data class NoticeSearchComparison(
    val baseline: List<NoticeSource>,
    val ranked: List<NoticeSource>,
    val ranking: NoticeSearchRanking,
    val minSimilarity: Double,
    val embeddingCached: Boolean,
    val elapsedMs: Int,
    val usage: QuestionQuota,
)
