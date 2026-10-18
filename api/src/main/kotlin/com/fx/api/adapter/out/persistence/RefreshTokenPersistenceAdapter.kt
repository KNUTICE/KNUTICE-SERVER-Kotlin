package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.RefreshTokenRepository
import com.fx.api.application.port.out.user.RefreshTokenPersistencePort
import com.fx.api.domain.RefreshToken
import com.fx.common.annotation.PersistenceAdapter
import java.time.LocalDateTime

@PersistenceAdapter
class RefreshTokenPersistenceAdapter(
    private val refreshTokenRepository: RefreshTokenRepository,
) : RefreshTokenPersistencePort {

    override fun save(refreshToken: RefreshToken): RefreshToken =
        refreshTokenRepository.save(refreshToken)

    override fun findByTokenHashForUpdate(tokenHash: String): RefreshToken? =
        refreshTokenRepository.findByTokenHash(tokenHash)

    override fun findByPreviousTokenHash(tokenHash: String): RefreshToken? =
        refreshTokenRepository.findByPreviousTokenHash(tokenHash)

    override fun delete(refreshToken: RefreshToken) =
        refreshTokenRepository.delete(refreshToken)

    override fun deleteByTokenHash(tokenHash: String) {
        refreshTokenRepository.deleteByTokenHash(tokenHash)
    }

    override fun deleteExpired(now: LocalDateTime) {
        refreshTokenRepository.deleteExpired(now)
    }

}
