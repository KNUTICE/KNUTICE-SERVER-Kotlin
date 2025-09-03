package com.fx.api.application.port.out

import com.fx.api.domain.AuthenticatedUserInfo
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.UserRole

interface JwtProviderPort {

    fun generateTokens(userId: String, role: UserRole): TokenInfo

    fun validateToken(token: String): Boolean

    fun getAuthenticatedUserInfo(accessToken: String): AuthenticatedUserInfo

}