package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.fcmtoken.FcmToken
import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmToken, Long> {

    fun findByToken(token: String): FcmToken?

    fun existsByToken(token: String): Boolean

}
