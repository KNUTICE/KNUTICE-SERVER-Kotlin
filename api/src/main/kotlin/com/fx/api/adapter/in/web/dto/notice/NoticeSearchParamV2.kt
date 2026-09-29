package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.application.port.`in`.dto.NoticeSearchCommand
import org.springframework.data.domain.Pageable

/** v2 공지 목록 조회 조건. [topicId] 는 토픽 코드. */
data class NoticeSearchParamV2(
    val nttId: Long? = null,
    val topicId: Int? = null,
    val keyword: String? = null
) {

    /** 정렬은 커서와 같은 `nttId` 내림차순으로 고정이라 [pageable] 에서는 크기만 쓴다. */
    fun toCommand(pageable: Pageable) =
        NoticeSearchCommand(
            nttId = this.nttId,
            topicId = this.topicId,
            keyword = this.keyword,
            size = pageable.pageSize
        )

}
