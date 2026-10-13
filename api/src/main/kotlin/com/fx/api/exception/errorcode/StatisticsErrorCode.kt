package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class StatisticsErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    STATISTICS_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "조회 기간이 올바르지 않습니다."),
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}
