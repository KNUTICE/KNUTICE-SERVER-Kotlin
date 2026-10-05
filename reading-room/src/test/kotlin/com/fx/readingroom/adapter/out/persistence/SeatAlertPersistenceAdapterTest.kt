package com.fx.readingroom.adapter.out.persistence

import com.fx.persistence.MySqlContainerConfig
import com.fx.readingroom.adapter.out.persistence.repository.SeatAlertRepository
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.exception.DuplicateSeatAlertException
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 실제 MySQL 에서 좌석 알림 저장 · 조회 · 삭제를 검증한다.
 * `ddl-auto=validate` 이므로 엔티티 매핑과 Flyway 스키마가 어긋나면 컨텍스트가 뜨지 않는다.
 */
@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=validate"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, SeatAlertPersistenceAdapterTest.FixedClockConfig::class, SeatAlertPersistenceAdapter::class)
class SeatAlertPersistenceAdapterTest @Autowired constructor(
    private val seatAlertPersistenceAdapter: SeatAlertPersistenceAdapter,
    private val seatAlertRepository: SeatAlertRepository,
    private val entityManager: EntityManager,
) {

    @TestConfiguration(proxyBeanMethods = false)
    class FixedClockConfig {
        @Bean
        fun clock(): Clock =
            Clock.fixed(Instant.parse("2026-09-30T01:00:00Z"), ZoneId.of("Asia/Seoul"))
    }

    private val now = LocalDateTime.of(2026, 9, 30, 10, 0)

    @Test
    fun `활성 알림 조회는 만료되지 않은 알림만 최신순으로 읽는다`() {
        val older = seatAlertPersistenceAdapter.create(SeatAlert.create(TOKEN_ID, ReadingRoom.ROOM1, 1, now.minusHours(2)))
        val newer = seatAlertPersistenceAdapter.create(SeatAlert.create(TOKEN_ID, ReadingRoom.ROOM2, 2, now.minusHours(1)))
        seatAlertRepository.saveAndFlush(SeatAlert(TOKEN_ID, ReadingRoom.ROOM3, 3, expiresAt = now))
        seatAlertPersistenceAdapter.create(SeatAlert.create(OTHER_TOKEN_ID, ReadingRoom.ROOM1, 1, now))
        flushAndClear()

        val active = seatAlertPersistenceAdapter.findActiveByFcmTokenId(TOKEN_ID, now)

        assertThat(active.map {
            it.id
        }).containsExactly(newer.id, older.id)
    }

    @Test
    fun `같은 좌석에 활성 알림이 있으면 중복 예외가 발생한다`() {
        seatAlertPersistenceAdapter.create(SeatAlert.create(TOKEN_ID, ReadingRoom.ROOM1, 1, now))

        assertThatThrownBy {
            seatAlertPersistenceAdapter.create(SeatAlert.create(TOKEN_ID, ReadingRoom.ROOM1, 1, now))
        }.isInstanceOf(DuplicateSeatAlertException::class.java)
    }

    @Test
    fun `같은 좌석의 만료된 알림은 지우고 새로 저장한다`() {
        seatAlertRepository.saveAndFlush(SeatAlert(TOKEN_ID, ReadingRoom.ROOM1, 1, expiresAt = now.minusMinutes(1)))

        val created = seatAlertPersistenceAdapter.create(SeatAlert.create(TOKEN_ID, ReadingRoom.ROOM1, 1, now))
        flushAndClear()

        assertThat(seatAlertRepository.findAll().map {
            it.id
        }).containsExactly(created.id)
    }

    @Test
    fun `내 알림만 지울 수 있다`() {
        val alert = seatAlertPersistenceAdapter.create(SeatAlert.create(TOKEN_ID, ReadingRoom.ROOM1, 1, now))
        val alertId = requireNotNull(alert.id)
        flushAndClear()

        assertThat(seatAlertPersistenceAdapter.deleteByIdAndFcmTokenId(alertId, OTHER_TOKEN_ID)).isFalse()
        assertThat(seatAlertPersistenceAdapter.deleteByIdAndFcmTokenId(alertId, TOKEN_ID)).isTrue()
        assertThat(seatAlertRepository.findById(alertId)).isEmpty()
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    companion object {
        private const val TOKEN_ID = 1L
        private const val OTHER_TOKEN_ID = 2L
    }

}
