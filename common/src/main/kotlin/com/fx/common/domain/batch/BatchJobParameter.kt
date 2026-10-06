package com.fx.common.domain.batch

import com.fx.common.domain.TopicType

/** Job 이 받는 업무 파라미터. 값은 모두 문자열로 저장되므로 형식을 여기서 검증한다. */
enum class BatchJobParameter(
    val parameterName: String,
) {

    /** `noticeCrawlJob` 이 크롤링할 게시판 유형. 학식은 게시판이 아니라 받지 않는다. */
    TOPIC_TYPE("topicType") {
        override fun validate(value: String) {
            require(value in CRAWLABLE_TOPIC_TYPES) {
                "topicType 은 ${CRAWLABLE_TOPIC_TYPES.joinToString(" / ")} 중 하나여야 합니다: $value"
            }
        }
    },

    /** `maintenanceJob` 이 남길 실행 기록 보존 일수 */
    RETENTION_DAYS("retentionDays") {
        override fun validate(value: String) {
            val days = value.toIntOrNull()
            require(days != null && days >= 1) {
                "retentionDays 는 1 이상의 정수여야 합니다: $value"
            }
        }
    },
    ;

    /** @throws IllegalArgumentException 값의 형식이 잘못됐을 때 */
    abstract fun validate(value: String)

    private companion object {

        val CRAWLABLE_TOPIC_TYPES: List<String> =
            listOf(TopicType.NOTICE.name, TopicType.MAJOR.name)

    }

}
