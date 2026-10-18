package com.fx.api.domain

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime
import java.util.Base64
import java.util.HexFormat

/** 토큰 해시 길이 — SHA-256 hex. `token_hash` · `previous_token_hash` 컬럼 길이와 같아야 한다. */
const val REFRESH_TOKEN_HASH_LENGTH = 64

/**
 * 관리자 로그인 세션의 리프레시 토큰.
 *
 * 로그인 한 번이 한 행이다. 재발급할 때마다 토큰을 새것으로 바꾸고(회전) 만료를 연장한다.
 * 토큰 원문은 저장하지 않고 해시만 둔다. 직전 토큰의 해시를 남겨 두어, 이미 바뀐 토큰이 다시 들어오면 탈취로 보고 세션을 끊는다.
 */
@Entity
@Table(
    name = "refresh_token",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_refresh_token_token_hash", columnNames = ["token_hash"]),
    ],
    indexes = [
        // 재사용 감지 : `WHERE previous_token_hash = ?`
        Index(name = "idx_refresh_token_previous_token_hash", columnList = "previous_token_hash"),
        // 만료 정리 : `WHERE expires_at <= ?`
        Index(name = "idx_refresh_token_expires_at", columnList = "expires_at"),
    ],
)
class RefreshToken(
    userId: Long,
    tokenHash: String,
    expiresAt: LocalDateTime,
) : BaseEntity() {

    @Column(name = "user_id", nullable = false, updatable = false, comment = "사용자 ID")
    val userId: Long = userId

    @Column(name = "token_hash", nullable = false, length = REFRESH_TOKEN_HASH_LENGTH, comment = "현재 토큰의 SHA-256 (hex)")
    var tokenHash: String = tokenHash
        protected set

    @Column(name = "previous_token_hash", length = REFRESH_TOKEN_HASH_LENGTH, comment = "직전 토큰의 SHA-256 (hex). 다시 들어오면 재사용으로 본다")
    var previousTokenHash: String? = null
        protected set

    @Column(name = "expires_at", nullable = false, comment = "만료 시각. 재발급할 때마다 연장한다")
    var expiresAt: LocalDateTime = expiresAt
        protected set

    fun isExpired(now: LocalDateTime): Boolean =
        !now.isBefore(expiresAt)

    /** 새 토큰으로 바꾸고 만료를 [now] 부터 [lifetime] 뒤로 늦춘다. 바뀌기 전 토큰은 재사용 감지에 쓰도록 남긴다. */
    fun rotate(newToken: String, now: LocalDateTime, lifetime: Duration) {
        previousTokenHash = tokenHash
        tokenHash = hash(newToken)
        expiresAt = now.plus(lifetime)
    }

    companion object {

        private const val TOKEN_BYTES = 32

        private val secureRandom = SecureRandom()

        fun issue(userId: Long, token: String, now: LocalDateTime, lifetime: Duration): RefreshToken =
            RefreshToken(userId, hash(token), expiresAt = now.plus(lifetime))

        /** 추측할 수 없는 토큰 원문. 무작위 32바이트를 URL-safe Base64 로 쓴다. */
        fun generateToken(): String {
            val bytes = ByteArray(TOKEN_BYTES)
            secureRandom.nextBytes(bytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }

        /** 원문이 무작위 32바이트라 비밀번호처럼 솔트 · 느린 해시를 쓰지 않아도 원문을 되찾을 수 없다. */
        fun hash(token: String): String =
            HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
            )

    }

}
