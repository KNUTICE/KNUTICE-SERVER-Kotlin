package com.fx.common.utils

import com.fx.common.domain.CrawlableType
import com.fx.common.domain.MajorType
import com.fx.common.domain.MealType
import com.fx.common.domain.NoticeType
import com.fx.common.domain.TopicType
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.TopicErrorCode

object TopicUtils {

    @JvmStatic
    fun parseToCrawlable(topic: String, topicType: TopicType): CrawlableType {
        return try {
            when (topicType) {
                TopicType.NOTICE -> NoticeType.valueOf(topic)
                TopicType.MAJOR -> MajorType.valueOf(topic)
                TopicType.MEAL -> MealType.valueOf(topic)
            }
        } catch (e: IllegalArgumentException) {
            throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
        }
    }

}
