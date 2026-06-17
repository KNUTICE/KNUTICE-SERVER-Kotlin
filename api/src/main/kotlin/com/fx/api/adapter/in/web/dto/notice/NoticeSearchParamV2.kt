package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MealType
import com.fx.global.exception.TopicException
import com.fx.global.exception.errorcode.TopicErrorCode
import org.springframework.data.domain.Pageable

data class NoticeSearchParamV2(

    val nttId: Long? = null,
    val topicId: Int? = null,
    val keyword: String? = null

) {
    fun toCommand(pageable: Pageable) =
        NoticeQuery(
            nttId = this.nttId,
            topic = topicId?.let { CrawlableType.fromCode(it) },
            keyword = this.keyword,
            pageable = pageable
        )
}
