package com.fx.api.adapter.`in`.web.user.dto

import com.fx.api.domain.TokenInfo

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
) {

    companion object {
        @JvmStatic
        fun from(tokenInfo: TokenInfo): TokenResponse =
            TokenResponse(
                accessToken = tokenInfo.accessToken,
                refreshToken = tokenInfo.refreshToken,
            )
    }

}