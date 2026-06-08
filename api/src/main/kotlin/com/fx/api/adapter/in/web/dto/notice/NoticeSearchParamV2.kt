package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MealType
import com.fx.global.exception.TopicException
import com.fx.global.exception.errorcode.TopicErrorCode
import org.springframework.data.domain.Pageable

data class NoticeSearchParamV2(

    val nttId: Long? = null,
    val topic: Int? = null,
    val keyword: String? = null

) {
    fun toCommand(pageable: Pageable) =
        NoticeQuery(
            nttId = this.nttId,
            topic = topic?.let { resolveTopicCode(it) },
            keyword = this.keyword,
            pageable = pageable
        )

    private fun resolveTopicCode(code: Int): CrawlableType {
        val type = try {
            CrawlableType.fromCode(code)
        } catch (e: IllegalArgumentException) {
            throw TopicException(TopicErrorCode.INVALID_TOPIC_CODE, e)
        }
        if (type is MealType) throw TopicException(TopicErrorCode.INVALID_TOPIC_CODE)
        return type
    }
}
