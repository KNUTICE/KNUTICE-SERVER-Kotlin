package com.fx.api.application.port.`in`.question.dto

/**
 * @property askerKey 질문한 관리자 user.id
 * @property decayPerYear 이번 질문의 검색 설정 (null 이면 기본값)
 * @property titlePenalty 이번 질문의 검색 설정 (null 이면 기본값)
 * @property topNotices 근거 공지 수 (null 이면 기본값)
 * @property generateAnswer false 면 검색만 하고 답변 모델을 부르지 않는다
 */
data class NoticeQuestionCommand(
    val askerKey: String,
    val question: String,
    val decayPerYear: Double? = null,
    val titlePenalty: Double? = null,
    val topNotices: Int? = null,
    val generateAnswer: Boolean = true,
)
