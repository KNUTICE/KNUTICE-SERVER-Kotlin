package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.domain.CursorPage
import com.fx.common.domain.batch.BatchRunRequest

data class BatchRunRequestPageResponse(
    val items: List<BatchRunRequestResponse>,
    /** 다음 페이지 커서 (요청 id). 마지막 페이지면 null */
    val nextCursor: String?,
) {

    companion object {

        fun from(page: CursorPage<BatchRunRequest>): BatchRunRequestPageResponse =
            BatchRunRequestPageResponse(
                items = page.items.map {
                    BatchRunRequestResponse.from(it)
                },
                nextCursor = page.nextCursor?.toString(),
            )

    }

}
