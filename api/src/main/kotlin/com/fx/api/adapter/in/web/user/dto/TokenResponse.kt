package com.fx.api.adapter.`in`.web.user.dto

import com.fx.api.domain.TokenInfo

/** 리프레시 토큰은 본문이 아니라 HttpOnly 쿠키로 보낸다. */
data class TokenResponse(
    val accessToken: String,
) {

    companion object {
        @JvmStatic
        fun from(tokenInfo: TokenInfo): TokenResponse =
            TokenResponse(
                accessToken = tokenInfo.accessToken,
            )
    }

}
