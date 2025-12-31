package com.fx.global.utils

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.domain.TopicType
import com.fx.global.exception.TopicException
import com.fx.global.exception.errorcode.TopicErrorCode

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
