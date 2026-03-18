package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.readingrooms.CreateSeatAlertRequest
import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomSeatResponse
import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomStatusResponse
import com.fx.api.adapter.`in`.web.dto.readingrooms.SeatAlertResponse
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.readingroom.domain.ReadingRoom
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.readingroom.exception.errorcode.ReadingRoomErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "열람실 API")
interface ReadingRoomOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "토큰 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "열람실 정보 조회", description = "열람실에 대한 정보를 조회합니다. <br>" +
            "행/열 정보는 Front 렌더링에 사용됩니다.")
    suspend fun getReadingRoomStatus(
        @RequestHeader fcmToken: String
    ): ResponseEntity<Api<List<ReadingRoomStatusResponse>>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "토큰 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "열람실 좌석 조회", description = "각 열람실의 좌석 정보를 조회합니다. <br>" +
            "각 좌석의 이용여부가 응답되며, 사용중인 경우 사용자의 마스킹된 이름이 제공됩니다. <br>" +
            "**사용중이 아닌 경우 사용자 이름은 null 값이 반환됩니다.** <br>" +
            "X: colume (열) <br>" +
            "Y: row (행)")
    suspend fun getReadingRoomSeats(
        @RequestHeader fcmToken: String,
        @PathVariable readingRoom: ReadingRoom
    ): ResponseEntity<Api<List<ReadingRoomSeatResponse>>>


    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "토큰 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "좌석 조회 실패",
                description = "좌석이 존재하지 않는 경우",
                value = ReadingRoomErrorCode::class,
                constant = "SEAT_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "좌석 알림 등록 실패1",
                description = "좌석이 비어있는 경우",
                value = ReadingRoomErrorCode::class,
                constant = "SEAT_ALREADY_AVAILABLE"
            ),
            ApiExceptionExplanation(
                name = "좌석 알림 등록 실패2",
                description = "좌석 알림 등록 개수 초과 (최대 5개)",
                value = ReadingRoomErrorCode::class,
                constant = "MAX_SEAT_ALERT_LIMIT_EXCEEDED"
            ),
        ]
    )
    @Operation(summary = "빈자리 알림 등록", description = "빈자리 알림을 등록합니다. <br>" +
            "빈자리 알림 등록은 **최대 5개**까지 가능합니다")
    suspend fun createSeatAlert(
        @RequestHeader fcmToken: String,
        @RequestBody createSeatAlertRequest: CreateSeatAlertRequest
    ): ResponseEntity<Api<SeatAlertResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "토큰 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "빈자리 알림 조회", description = "등록된 빈자리 알림을 조회합니다. <br>" +
            "알림이 활성화된 좌석에 대한 정보가 반환됩니다.")
    suspend fun getSeatAlerts(
        @RequestHeader fcmToken: String
    ): ResponseEntity<Api<List<SeatAlertResponse>>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "토큰 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "빈자리 알림 삭제 실패",
                description = "좌석이 존재하지 않는 경우",
                value = ReadingRoomErrorCode::class,
                constant = "SEAT_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "빈자리 알림 삭제", description = "등록된 빈자리 알림을 삭제합니다.")
    suspend fun deleteSeatAlert(
        @RequestHeader fcmToken: String,
        @PathVariable alertId: String
    ): ResponseEntity<Api<Boolean>>

}