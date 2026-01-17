package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomSeatResponse
import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomStatusResponse
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "열람실 API")
interface ReadingRoomOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "Topic 조회 실패",
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
                name = "Topic 조회 실패",
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
        @PathVariable roomId: Int
    ): ResponseEntity<Api<List<ReadingRoomSeatResponse>>>

}