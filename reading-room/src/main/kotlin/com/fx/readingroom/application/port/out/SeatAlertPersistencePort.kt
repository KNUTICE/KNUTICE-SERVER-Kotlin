package com.fx.readingroom.application.port.out

import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlertTarget
import com.fx.readingroom.domain.exception.DuplicateSeatAlertException
import java.time.LocalDateTime

interface SeatAlertPersistencePort {

    /**
     * 알림을 저장한다. 같은 좌석의 만료된 알림이 아직 지워지지 않았으면 먼저 지운다.
     * @throws DuplicateSeatAlertException 같은 좌석의 활성 알림이 이미 있을 때
     */
    fun create(seatAlert: SeatAlert): SeatAlert

    /** 토큰의 만료되지 않은 알림. 최근 등록 순. */
    fun findActiveByFcmTokenId(fcmTokenId: Long, now: LocalDateTime): List<SeatAlert>

    /** 토큰 소유의 알림을 지운다. 지운 알림이 없으면 false. */
    fun deleteByIdAndFcmTokenId(seatAlertId: Long, fcmTokenId: Long): Boolean

    /** 만료된 알림을 모두 지우고 지운 수를 돌려준다. */
    fun deleteExpired(now: LocalDateTime): Int

    /** 만료되지 않은 전체 알림과 발송할 토큰. 토큰이 지워진 알림은 빠진다. */
    fun findActiveTargets(now: LocalDateTime): List<SeatAlertTarget>

    fun deleteById(seatAlertId: Long)

}
