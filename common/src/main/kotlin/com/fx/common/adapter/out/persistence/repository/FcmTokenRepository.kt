package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.domain.fcmtoken.FcmTokenRef
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface FcmTokenRepository : JpaRepository<FcmToken, Long> {

    fun findByToken(token: String): FcmToken?

    fun existsByToken(token: String): Boolean

    @Query("SELECT new com.fx.common.domain.fcmtoken.FcmTokenRef(t.id, t.token) FROM FcmToken t WHERE t.token IN :tokens")
    fun findRefsByTokenIn(tokens: Collection<String>): List<FcmTokenRef>

    /** 발송에 실패한(등록이 풀린) 토큰을 한 번에 비활성화한다. */
    @Modifying
    @Query("UPDATE FcmToken t SET t.isActive = false, t.updatedAt = :now WHERE t.id IN :ids AND t.isActive = true")
    fun deactivateAll(ids: Collection<Long>, now: LocalDateTime): Int

}
