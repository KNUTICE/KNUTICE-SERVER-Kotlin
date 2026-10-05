package com.fx.crawler.domain.push

/**
 * 토픽 하나의 발송 계획. 발송 Step 은 토픽마다 파티션을 나눠 구독자에게 [messages] 를 보낸다.
 * @property noticeIds 발송을 마치면 발송 완료로 표시할 공지 (학식은 비어 있다)
 */
data class TopicPushPlan(
    val topicCode: Int,
    val messages: LocalizedPushMessages,
    val noticeIds: List<Long> = emptyList(),
)
