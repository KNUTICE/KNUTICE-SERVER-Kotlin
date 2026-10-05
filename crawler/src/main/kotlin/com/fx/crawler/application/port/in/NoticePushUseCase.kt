package com.fx.crawler.application.port.`in`

import com.fx.common.domain.TopicType
import com.fx.crawler.domain.push.TopicPushPlan

interface NoticePushUseCase {

    /** 발송 대기 중인 [topicType] 공지를 토픽별 발송 계획으로 만든다. */
    fun preparePushPlans(topicType: TopicType): List<TopicPushPlan>

    /** 토픽 발송을 마친 공지를 발송 완료로 표시한다. */
    fun markNotified(noticeIds: List<Long>)

}
