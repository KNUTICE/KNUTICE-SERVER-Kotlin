package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.RefreshToken
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    /** `SELECT ... FOR UPDATE`. 같은 토큰으로 동시에 재발급하면 뒤의 요청은 앞의 요청이 커밋한 뒤 바뀐 토큰을 보고 찾지 못한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByTokenHash(tokenHash: String): RefreshToken?

    fun findByPreviousTokenHash(previousTokenHash: String): RefreshToken?

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.tokenHash = :tokenHash")
    fun deleteByTokenHash(@Param("tokenHash") tokenHash: String): Int

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt <= :now")
    fun deleteExpired(@Param("now") now: LocalDateTime): Int

}
