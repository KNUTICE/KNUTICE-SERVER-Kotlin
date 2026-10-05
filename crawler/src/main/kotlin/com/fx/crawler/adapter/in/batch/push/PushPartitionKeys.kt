package com.fx.crawler.adapter.`in`.batch.push

/** 토픽 발송 파티션의 ExecutionContext 키. */
object PushPartitionKeys {

    const val TOPIC_CODE = "topicCode"

    /** 언어별 알림 (`LocalizedPushMessages` JSON) */
    const val MESSAGES = "messages"

    /** 발송을 마치면 발송 완료로 표시할 공지 ID (JSON 배열) */
    const val NOTICE_IDS = "noticeIds"

}
