package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.adapter.out.persistence.repository.FcmTokenRepository
import com.fx.common.domain.DeviceType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.crawler.support.CrawlerIntegrationTest
import com.fx.readingroom.adapter.out.persistence.repository.SeatAlertRepository
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDateTime

class SeatAlertCheckJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("seatAlertCheckJob") lateinit var seatAlertCheckJob: Job
    @Autowired lateinit var fcmTokenRepository: FcmTokenRepository
    @Autowired lateinit var seatAlertRepository: SeatAlertRepository

    @Test
    fun `만료 알림을 지우고 빈 좌석 알림만 보낸 뒤 지운다`() {
        val koToken = requireNotNull(fcmTokenRepository.save(FcmToken("token-ko", DeviceType.iOS, "ko")).id)
        val enToken = requireNotNull(fcmTokenRepository.save(FcmToken("token-en", DeviceType.iOS, "en")).id)
        val now = LocalDateTime.now()

        val expired = seatAlertRepository.save(SeatAlert(koToken, ReadingRoom.ROOM1, 1, expiresAt = now.minusMinutes(1)))
        val available = seatAlertRepository.save(SeatAlert(koToken, ReadingRoom.ROOM1, 10, expiresAt = now.plusHours(1)))
        val occupied = seatAlertRepository.save(SeatAlert(enToken, ReadingRoom.ROOM1, 11, expiresAt = now.plusHours(1)))
        val roomFailed = seatAlertRepository.save(SeatAlert(koToken, ReadingRoom.ROOM2, 5, expiresAt = now.plusHours(1)))
        readingRoomRemotePort.availableSeats[ReadingRoom.ROOM1] = setOf(1, 10)

        val execution = run(seatAlertCheckJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        val sent = pushPort.sent.single()
        assertThat(sent.tokens).containsExactly("token-ko")
        assertThat(sent.message!!.title).isEqualTo("빈자리 알림")
        assertThat(sent.message!!.body).isEqualTo("제1집중 학습 ZONE 10번 좌석이 비었습니다!")
        assertThat(seatAlertRepository.findAll().map { it.id }).containsExactlyInAnyOrder(occupied.id, roomFailed.id)
        assertThat(listOf(expired.id, available.id)).noneMatch { seatAlertRepository.existsById(requireNotNull(it)) }
    }

    @Test
    fun `알림이 없으면 열람실 사이트를 조회하지 않는다`() {
        val execution = run(seatAlertCheckJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(pushPort.sent).isEmpty()
    }

}
