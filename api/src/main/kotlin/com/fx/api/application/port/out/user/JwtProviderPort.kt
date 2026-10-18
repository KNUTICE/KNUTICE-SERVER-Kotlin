package com.fx.api.application.port.out.user

import com.fx.api.domain.AuthenticatedUserInfo
import com.fx.api.domain.UserRole

interface JwtProviderPort {

    fun generateAccessToken(userId: String, role: UserRole): String

    /**
     * 액세스 토큰의 서명 · 만료 · 종류를 확인하고 사용자 정보를 꺼낸다.
     *
     * @throws com.fx.api.exception.AuthException 만료된 토큰 (EXPIRED_TOKEN), 그 밖의 잘못된 토큰 (INVALID_TOKEN)
     */
    fun authenticate(accessToken: String): AuthenticatedUserInfo

}
