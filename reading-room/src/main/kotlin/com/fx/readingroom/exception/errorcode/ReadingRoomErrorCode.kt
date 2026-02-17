package com.fx.readingroom.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class ReadingRoomErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석이 존재하지 않습니다."),
    SEAT_ALREADY_AVAILABLE(HttpStatus.BAD_REQUEST, "이미 이용 가능한 좌석입니다."),
    SEAT_ALERT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 좌석에 대한 알림이 등록되어 있습니다."),
    MAX_SEAT_ALERT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "좌석 알림은 최대 5개까지 등록할 수 있습니다."),
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}