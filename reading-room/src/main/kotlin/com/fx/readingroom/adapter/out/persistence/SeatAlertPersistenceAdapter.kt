package com.fx.readingroom.adapter.out.persistence

import com.fx.common.annotation.PersistenceAdapter
import com.fx.readingroom.adapter.out.persistence.repository.SeatAlertRepository
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.exception.DuplicateSeatAlertException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@PersistenceAdapter
class SeatAlertPersistenceAdapter(
    private val seatAlertRepository: SeatAlertRepository,
    private val clock: Clock,
) : SeatAlertPersistencePort {

    @Transactional
    override fun create(seatAlert: SeatAlert): SeatAlert {
        // 만료됐지만 정리 배치가 아직 지우지 않은 알림이 유니크 제약을 막지 않게 한다
        seatAlertRepository.deleteExpiredSeat(
            fcmTokenId = seatAlert.fcmTokenId,
            readingRoom = seatAlert.readingRoom,
            seatNumber = seatAlert.seatNumber,
            now = LocalDateTime.now(clock),
        )
        return try {
            seatAlertRepository.saveAndFlush(seatAlert)
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateSeatAlertException(e)
        }
    }

    @Transactional(readOnly = true)
    override fun findActiveByFcmTokenId(fcmTokenId: Long, now: LocalDateTime): List<SeatAlert> =
        seatAlertRepository.findAllByFcmTokenIdAndExpiresAtAfterOrderByIdDesc(fcmTokenId, now)

    @Transactional
    override fun deleteByIdAndFcmTokenId(seatAlertId: Long, fcmTokenId: Long): Boolean =
        seatAlertRepository.deleteByIdAndFcmTokenId(seatAlertId, fcmTokenId) > 0

}
