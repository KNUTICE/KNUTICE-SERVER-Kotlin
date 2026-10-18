package com.fx.api.application.port.out.user

import com.fx.api.domain.RefreshToken
import java.time.LocalDateTime

interface RefreshTokenPersistencePort {

    fun save(refreshToken: RefreshToken): RefreshToken

    /** 현재 토큰으로 찾는다. 같은 토큰의 재발급이 겹치지 않도록 트랜잭션이 끝날 때까지 행을 잠근다. */
    fun findByTokenHashForUpdate(tokenHash: String): RefreshToken?

    /** 이미 다른 토큰으로 바뀐 직전 토큰으로 찾는다 (재사용 감지). */
    fun findByPreviousTokenHash(tokenHash: String): RefreshToken?

    fun delete(refreshToken: RefreshToken)

    fun deleteByTokenHash(tokenHash: String)

    fun deleteExpired(now: LocalDateTime)

}
