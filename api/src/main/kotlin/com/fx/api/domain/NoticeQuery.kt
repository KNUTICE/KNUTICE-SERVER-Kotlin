package com.fx.api.domain

/**
 * 공지 목록 조회 조건. `nttId` 보다 작은 공지를 `nttId` 내림차순으로 [size] 개 읽는다 (커서 방식).
 * @property topicCode 토픽 필터. null 이면 전체
 * @property keyword 제목 검색어. null 이면 검색하지 않는다
 */
data class NoticeQuery(
    val nttId: Long? = null,
    val topicCode: Int? = null,
    val keyword: String? = null,
    val size: Int,
)
