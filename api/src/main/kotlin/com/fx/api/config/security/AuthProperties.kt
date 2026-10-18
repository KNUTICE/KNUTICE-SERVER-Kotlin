package com.fx.api.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * 관리자 로그인 토큰 설정 (`auth.*`).
 *
 * @property jwtSecretKey 액세스 토큰(JWT) 서명 키. HMAC-SHA 키라 32바이트 이상이어야 한다
 * @property accessTokenTtl 액세스 토큰 유효 시간. 서버에서 폐기할 수 없으므로 권한 회수가 이 시간 안에 반영된다
 * @property refreshTokenTtl 리프레시 토큰 유효 시간. 재발급할 때마다 그 시각부터 다시 센다
 * @property refreshTokenCookieSecure 리프레시 토큰 쿠키의 Secure 속성. https 가 아닌 로컬에서 쿠키가 저장되지 않을 때만 끈다
 */
@ConfigurationProperties("auth")
data class AuthProperties(
    val jwtSecretKey: String,
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration,
    val refreshTokenCookieSecure: Boolean = true,
)
