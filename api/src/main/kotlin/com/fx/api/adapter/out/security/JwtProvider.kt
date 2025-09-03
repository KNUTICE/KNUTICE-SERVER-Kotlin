package com.fx.api.adapter.out.security

import com.fx.api.application.port.out.JwtProviderPort
import com.fx.api.domain.AuthenticatedUserInfo
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.UserRole
import com.fx.api.exception.JwtException
import com.fx.api.exception.errorcode.JwtErrorCode
import com.fx.global.annotation.SecurityAdapter
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.toString

@SecurityAdapter
class JwtProvider(
    @Value("\${jwt.secret.key}") private val secretKey: String,
    @Value("\${jwt.access-token.plus-hour}") private val accessTokenPlusHour: Long,
    @Value("\${jwt.refresh-token.plus-hour}") private val refreshTokenPlusHour: Long,
) : JwtProviderPort {

    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    override fun generateTokens(userId: String, role: UserRole): TokenInfo {
        val accessToken = createToken(userId, role, accessTokenPlusHour)
        val refreshToken = createToken(userId, role, refreshTokenPlusHour)
        // TODO refresh token 저장

        return TokenInfo(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    override fun getAuthenticatedUserInfo(accessToken: String): AuthenticatedUserInfo {
        val claims = parseTokenSafely(accessToken)

        val userId = claims["userId"].toString()
        val role = UserRole.valueOf(claims["role"].toString())

        return AuthenticatedUserInfo(
            userId = userId,
            role = role
        )
    }

    override fun validateToken(token: String): Boolean {
        parseTokenSafely(token) // 예외 없으면 유효한 토큰
        return true
    }

    private fun parseTokenSafely(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            throw when (e) {
                is SignatureException -> JwtException(JwtErrorCode.INVALID_TOKEN)
                is ExpiredJwtException -> JwtException(JwtErrorCode.EXPIRED_TOKEN)
                else -> JwtException(JwtErrorCode.TOKEN_EXCEPTION)
            }
        }
    }

    private fun createToken(userId: String, role: UserRole, expireHour: Long): String {
        val claims = mutableMapOf<String, Any>()
        claims["userId"] = userId
        claims["role"] = role.toString()

        val expiredLocalDateTime = LocalDateTime.now().plusHours(expireHour)
        val expiredAt = Date.from(expiredLocalDateTime.atZone(ZoneId.systemDefault()).toInstant())

        return Jwts.builder()
            .signWith(key)
            .claims(claims)
            .expiration(expiredAt)
            .compact()
    }

}