package com.fx.crawler.domain.push

/**
 * 발송 결과.
 * @property deactivatedTokenIds 등록이 풀려 비활성화한 토큰
 */
data class PushSendResult(
    val targetCount: Int,
    val deactivatedTokenIds: Set<Long>,
) {

    val hasInvalidToken: Boolean
        get() = deactivatedTokenIds.isNotEmpty()

}
