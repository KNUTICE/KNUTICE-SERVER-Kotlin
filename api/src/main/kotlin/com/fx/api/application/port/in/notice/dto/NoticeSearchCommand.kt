package com.fx.api.application.port.`in`.notice.dto

/**
 * 공지 목록 조회. 토픽은 v1 은 [topicName], v2 는 [topicId] 로 지정한다 (둘 다 없으면 전체).
 * @property nttId 커서. 이 값보다 작은 공지를 읽는다
 */
data class NoticeSearchCommand(
    val nttId: Long? = null,
    val topicName: String? = null,
    val topicId: Int? = null,
    val keyword: String? = null,
    val size: Int,
)
