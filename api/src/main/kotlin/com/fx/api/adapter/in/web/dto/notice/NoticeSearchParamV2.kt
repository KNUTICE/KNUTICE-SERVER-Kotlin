package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.domain.NoticeQuery
import com.fx.common.domain.CrawlableType
import com.fx.common.domain.MealType
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.TopicErrorCode
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
