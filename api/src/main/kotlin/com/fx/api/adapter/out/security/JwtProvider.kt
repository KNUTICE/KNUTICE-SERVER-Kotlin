package com.fx.api.adapter.out.security

import com.fx.api.application.port.out.user.JwtProviderPort
import com.fx.api.config.security.AuthProperties
import com.fx.api.domain.AuthenticatedUserInfo
import com.fx.api.domain.UserRole
import com.fx.api.exception.AuthException
import com.fx.api.exception.errorcode.AuthErrorCode
import com.fx.common.annotation.SecurityAdapter
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Clock
import java.util.Date

/**
 * 관리자 API 액세스 토큰(JWT, HMAC-SHA).
 *
 * 리프레시 토큰은 JWT 가 아니라 DB 에 저장하는 무작위 값이다 (`RefreshToken`).
 * [TOKEN_TYPE_CLAIM] 이 없는 토큰은 받지 않는다. 리프레시 토큰도 JWT 로 발급하던 때의 토큰을 액세스 토큰으로 쓰지 못하게 하기 위해서다.
 */
@SecurityAdapter
class JwtProvider(
    private val authProperties: AuthProperties,
    private val clock: Clock,
) : JwtProviderPort {

    private val key = Keys.hmacShaKeyFor(authProperties.jwtSecretKey.toByteArray())

    private val parser = Jwts.parser()
        .verifyWith(key)
        .clock {
            Date.from(clock.instant())
        }
        .build()

    override fun generateAccessToken(userId: String, role: UserRole): String {
        val now = clock.instant()
        return Jwts.builder()
            .claim(USER_ID_CLAIM, userId)
            .claim(ROLE_CLAIM, role.name)
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(authProperties.accessTokenTtl)))
            .signWith(key)
            .compact()
    }

    override fun authenticate(accessToken: String): AuthenticatedUserInfo {
        val claims = parse(accessToken)
        if (claims[TOKEN_TYPE_CLAIM] != ACCESS_TOKEN_TYPE) {
            throw AuthException(AuthErrorCode.INVALID_TOKEN)
        }
        val userId = claims[USER_ID_CLAIM] as? String
        val role = UserRole.entries.find {
            it.name == claims[ROLE_CLAIM]
        }
        if (userId == null || role == null) {
            throw AuthException(AuthErrorCode.INVALID_TOKEN)
        }
        return AuthenticatedUserInfo(
            userId = userId,
            role = role,
        )
    }

    private fun parse(token: String): Claims =
        try {
            parser.parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            throw AuthException(AuthErrorCode.EXPIRED_TOKEN, e)
        } catch (e: JwtException) {
            throw AuthException(AuthErrorCode.INVALID_TOKEN, e)
        } catch (e: IllegalArgumentException) {
            throw AuthException(AuthErrorCode.INVALID_TOKEN, e)
        }

    companion object {

        private const val USER_ID_CLAIM = "userId"
        private const val ROLE_CLAIM = "role"
        private const val TOKEN_TYPE_CLAIM = "tokenType"
        private const val ACCESS_TOKEN_TYPE = "ACCESS"

    }

}
