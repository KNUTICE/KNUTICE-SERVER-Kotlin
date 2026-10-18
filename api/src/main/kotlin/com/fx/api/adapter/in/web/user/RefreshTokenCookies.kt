package com.fx.api.adapter.`in`.web.user

import com.fx.api.config.security.AuthProperties
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * 리프레시 토큰 쿠키.
 *
 * - HttpOnly: 스크립트가 읽지 못하므로 XSS 로 토큰을 빼낼 수 없다.
 * - SameSite=Strict: 다른 사이트에서 보낸 요청에는 실리지 않는다 (CSRF 방지).
 * - Path: 재발급 · 로그아웃 API 에만 보낸다.
 * - Max-Age: 서버의 세션 만료와 같다. 재발급할 때마다 새 쿠키로 덮어써 함께 연장된다.
 */
@Component
class RefreshTokenCookies(
    private val authProperties: AuthProperties,
) {

    fun create(refreshToken: String): ResponseCookie =
        cookie(refreshToken, authProperties.refreshTokenTtl)

    fun expire(): ResponseCookie =
        cookie("", Duration.ZERO)

    private fun cookie(value: String, maxAge: Duration): ResponseCookie =
        ResponseCookie.from(NAME, value)
            .httpOnly(true)
            .secure(authProperties.refreshTokenCookieSecure)
            .sameSite("Strict")
            .path(PATH)
            .maxAge(maxAge)
            .build()

    companion object {

        const val NAME = "refresh_token"

        private const val PATH = "/open-api/v1/users"

    }

}
