package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.common.domain.DeviceType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.persistence.withId
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.exception.DuplicateSeatAlertException
import com.fx.readingroom.exception.ReadingRoomException
import com.fx.readingroom.exception.errorcode.ReadingRoomErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReadingRoomCommandServiceTest : BehaviorSpec({

    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>()
    val readingRoomRemotePort = mockk<ReadingRoomRemotePort>()
    val seatAlertPersistencePort = mockk<SeatAlertPersistencePort>()
    val zone = ZoneId.of("Asia/Seoul")
    val clock = Clock.fixed(Instant.parse("2026-09-30T01:00:00Z"), zone)
    val now = LocalDateTime.now(clock)
    val service = ReadingRoomCommandService(fcmTokenPersistencePort, readingRoomRemotePort, seatAlertPersistencePort, clock)

    val command = CreateSeatAlertCommand(fcmToken = "fcmToken", readingRoom = ReadingRoom.ROOM1, seatNumber = 10)

    fun seat(number: Int, available: Boolean) = ReadingRoomSeat(
        roomId = ReadingRoom.ROOM1, seatNumber = number, row = 1, column = number,
        isAvailable = available, returnAt = now.plusHours(3),
    )

    fun givenSeats(vararg seats: ReadingRoomSeat) {
        clearMocks(fcmTokenPersistencePort, readingRoomRemotePort, seatAlertPersistencePort)
        every { fcmTokenPersistencePort.getByToken("fcmToken") } returns FcmToken("fcmToken", DeviceType.iOS).withId(1L)
        every { readingRoomRemotePort.getCsrfToken() } returns "csrf"
        every { readingRoomRemotePort.getReadingRoomSeats(ReadingRoom.ROOM1, "csrf") } returns seats.toList()
    }

    fun activeAlert(seatNumber: Int) = SeatAlert.create(1L, ReadingRoom.ROOM1, seatNumber, now.minusHours(1))

    Given("좌석 알림 등록") {

        When("사용 중인 좌석이고 등록한 알림이 없으면") {
            givenSeats(seat(10, available = false))
            every { seatAlertPersistencePort.findActiveByFcmTokenId(1L, now) } returns emptyList()
            val created = slot<SeatAlert>()
            every { seatAlertPersistencePort.create(capture(created)) } answers { created.captured }

            Then("12시간 뒤 만료되는 알림을 저장한다") {
                val alert = service.createSeatAlert(command)
                alert.fcmTokenId shouldBe 1L
                alert.readingRoom shouldBe ReadingRoom.ROOM1
                alert.seatNumber shouldBe 10
                alert.expiresAt shouldBe now.plusHours(12)
            }
        }

        When("좌석이 없으면") {
            givenSeats(seat(11, available = false))

            Then("SEAT_NOT_FOUND 예외가 발생한다") {
                shouldThrow<ReadingRoomException> { service.createSeatAlert(command) }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.SEAT_NOT_FOUND
            }
        }

        When("이미 비어 있는 좌석이면") {
            givenSeats(seat(10, available = true))

            Then("SEAT_ALREADY_AVAILABLE 예외가 발생하고 저장하지 않는다") {
                shouldThrow<ReadingRoomException> { service.createSeatAlert(command) }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.SEAT_ALREADY_AVAILABLE
                verify(exactly = 0) { seatAlertPersistencePort.create(any()) }
            }
        }

        When("활성 알림이 이미 최대 개수면") {
            givenSeats(seat(10, available = false))
            every { seatAlertPersistencePort.findActiveByFcmTokenId(1L, now) } returns (1..SeatAlert.MAX_ACTIVE_PER_TOKEN).map { activeAlert(100 + it) }

            Then("MAX_SEAT_ALERT_LIMIT_EXCEEDED 예외가 발생한다") {
                shouldThrow<ReadingRoomException> { service.createSeatAlert(command) }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.MAX_SEAT_ALERT_LIMIT_EXCEEDED
            }
        }

        When("같은 좌석에 활성 알림이 있으면") {
            givenSeats(seat(10, available = false))
            every { seatAlertPersistencePort.findActiveByFcmTokenId(1L, now) } returns listOf(activeAlert(10))

            Then("SEAT_ALERT_ALREADY_EXISTS 예외가 발생한다") {
                shouldThrow<ReadingRoomException> { service.createSeatAlert(command) }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.SEAT_ALERT_ALREADY_EXISTS
            }
        }

        When("동시 요청으로 저장 중 중복이 나면") {
            givenSeats(seat(10, available = false))
            every { seatAlertPersistencePort.findActiveByFcmTokenId(1L, now) } returns emptyList()
            every { seatAlertPersistencePort.create(any()) } throws DuplicateSeatAlertException()

            Then("SEAT_ALERT_ALREADY_EXISTS 예외로 바꾼다") {
                shouldThrow<ReadingRoomException> { service.createSeatAlert(command) }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.SEAT_ALERT_ALREADY_EXISTS
            }
        }
    }

    Given("좌석 알림 삭제") {

        When("내 알림이면") {
            givenSeats()
            every { seatAlertPersistencePort.deleteByIdAndFcmTokenId(500L, 1L) } returns true

            Then("삭제한다") {
                service.deleteSeatAlert("fcmToken", "500") shouldBe true
            }
        }

        When("없는 알림이거나 다른 토큰의 알림이면") {
            givenSeats()
            every { seatAlertPersistencePort.deleteByIdAndFcmTokenId(500L, 1L) } returns false

            Then("SEAT_NOT_FOUND 예외가 발생한다") {
                shouldThrow<ReadingRoomException> { service.deleteSeatAlert("fcmToken", "500") }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.SEAT_NOT_FOUND
            }
        }

        When("알림 ID 형식이 잘못되면") {
            givenSeats()

            Then("SEAT_NOT_FOUND 예외가 발생한다") {
                shouldThrow<ReadingRoomException> { service.deleteSeatAlert("fcmToken", "65a1b2c3d4e5f6a7b8c9d0e1") }
                    .baseErrorCode shouldBe ReadingRoomErrorCode.SEAT_NOT_FOUND
            }
        }
    }

})
