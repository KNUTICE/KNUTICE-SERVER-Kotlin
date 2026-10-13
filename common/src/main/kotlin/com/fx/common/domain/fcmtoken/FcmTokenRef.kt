package com.fx.common.domain.fcmtoken

/** 토큰 값과 토큰 ID 만 읽는 projection. 토큰 값을 ID 로 바꿀 때 엔티티 전체를 읽지 않도록 한다. */
data class FcmTokenRef(
    val id: Long,
    val token: String,
)
