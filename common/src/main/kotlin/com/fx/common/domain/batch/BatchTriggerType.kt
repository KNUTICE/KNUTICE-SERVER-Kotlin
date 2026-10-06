package com.fx.common.domain.batch

/** Job 을 어떻게 실행했는지. crawler 가 실행 파라미터 `triggerType` 으로 남기고 api 가 실행 이력에서 읽는다. */
enum class BatchTriggerType {

    /** 스케줄의 cron 으로 자동 실행 */
    SCHEDULED,

    /** 관리자의 수동 실행 요청 */
    MANUAL,

    ;

    companion object {

        /** 모르는 값이면 null */
        fun from(value: String?): BatchTriggerType? =
            entries.find {
                it.name == value
            }

    }

}
