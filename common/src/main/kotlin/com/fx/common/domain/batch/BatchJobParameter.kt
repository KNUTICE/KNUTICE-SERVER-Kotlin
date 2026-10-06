package com.fx.common.domain.batch

import com.fx.common.domain.TopicType

/** Job 이 받는 업무 파라미터. 값은 모두 문자열로 저장되므로 형식을 여기서 검증한다. */
enum class BatchJobParameter(
    val parameterName: String,
    /** 관리자 화면에 보여 줄 설명 */
    val description: String,
    /** 고를 수 있는 값. null 이면 직접 입력하고 [validate] 가 형식을 검증한다. */
    val allowedValues: List<String>?,
) {

    /** `noticeCrawlJob` 이 크롤링할 게시판 유형. 학식은 게시판이 아니라 받지 않는다. */
    TOPIC_TYPE("topicType", "크롤링할 게시판 유형 (NOTICE: 공지 게시판, MAJOR: 학과 게시판)", listOf(TopicType.NOTICE.name, TopicType.MAJOR.name)),

    /** `maintenanceJob` 이 남길 실행 기록 보존 일수 */
    RETENTION_DAYS("retentionDays", "실행 기록 보존 일수 (1 이상 정수)", null) {
        override fun validate(value: String) {
            val days = value.toIntOrNull()
            require(days != null && days >= 1) {
                "retentionDays 는 1 이상의 정수여야 합니다: $value"
            }
        }
    },
    ;

    /** @throws IllegalArgumentException 값의 형식이 잘못됐을 때 */
    open fun validate(value: String) {
        val allowed = allowedValues ?: return
        require(value in allowed) {
            "$parameterName 은 ${allowed.joinToString(" / ")} 중 하나여야 합니다: $value"
        }
    }

}
