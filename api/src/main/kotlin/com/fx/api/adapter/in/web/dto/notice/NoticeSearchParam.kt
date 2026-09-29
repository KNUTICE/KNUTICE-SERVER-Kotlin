package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.application.port.`in`.dto.NoticeSearchCommand
import org.springframework.data.domain.Pageable

/** v1 공지 목록 조회 조건. [topic] 은 토픽 이름 (예: `GENERAL_NEWS`). */
data class NoticeSearchParam(
    val nttId: Long? = null,
    val topic: String? = null,
    val keyword: String? = null
) {

    /** 정렬은 커서와 같은 `nttId` 내림차순으로 고정이라 [pageable] 에서는 크기만 쓴다. */
    fun toCommand(pageable: Pageable) =
        NoticeSearchCommand(
            nttId = this.nttId,
            topicName = this.topic,
            keyword = this.keyword,
            size = pageable.pageSize
        )

}
