package com.fx.api.adapter.`in`.web.user.dto

data class UserIdResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계). */
    val userId: String?
)
