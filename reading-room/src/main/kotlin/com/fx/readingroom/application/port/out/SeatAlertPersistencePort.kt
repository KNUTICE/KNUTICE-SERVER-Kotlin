package com.fx.readingroom.application.port.out

import com.fx.readingroom.domain.SeatAlert
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

}
