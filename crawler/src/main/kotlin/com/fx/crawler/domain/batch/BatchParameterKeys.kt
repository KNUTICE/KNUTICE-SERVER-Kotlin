package com.fx.crawler.domain.batch

import com.fx.common.domain.batch.BatchJobParameters

/** Job 파라미터 이름. */
object BatchParameterKeys {

    /** `noticeCrawlJob` 의 크롤링 대상 토픽 유형 (NOTICE / MAJOR) */
    const val TOPIC_TYPE = "topicType"

    /** `maintenanceJob` 의 메타데이터 보존 일수 */
    const val RETENTION_DAYS = "retentionDays"

    /** 자동 실행의 발화 시각. 발화마다 JobInstance 가 새로 생긴다 (identifying) */
    const val SCHEDULED_AT = "scheduledAt"

    /** 수동 실행 요청 ID. 요청마다 JobInstance 가 새로 생긴다 (identifying) */
    const val REQUEST_ID = "requestId"

    /** SCHEDULED / MANUAL (non-identifying) */
    const val TRIGGER_TYPE = "triggerType"

    /** 자동 실행한 스케줄 키 (non-identifying) */
    const val SCHEDULE_KEY = "scheduleKey"

    /** 수동 실행을 요청한 관리자 (non-identifying) */
    const val REQUESTED_BY = "requestedBy"

    /** 실행마다 달라지는 파라미터. 같은 작업인지 비교할 때 뺀다. 스케줄 · 요청 파라미터로는 쓸 수 없다. */
    val TRIGGER_KEYS: Set<String> = BatchJobParameters.RESERVED_NAMES

}
