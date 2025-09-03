package com.fx.api.domain

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String,
)