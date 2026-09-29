package com.fx.api.application.port.`in`

import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView

interface TopicQueryUseCase {

    /** 앱에 노출하는 [type] 토픽 전체. code 오름차순. */
    fun getTopics(type: TopicType): List<TopicView>

    /** [topicId] 가 있으면 코드로, 없으면 [topicName] 으로 토픽 하나를 찾는다. */
    fun getTopic(topicName: String?, topicId: Int?): TopicView

}
